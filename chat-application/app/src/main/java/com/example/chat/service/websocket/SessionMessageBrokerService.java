package com.example.chat.service.websocket;

import com.example.chat.event.websocket.WebSocketJsonMessageReceiveListener;
import com.example.chat.event.websocket.WebSocketTextMessageReceiveListener;
import com.example.chat.repository.SessionRepository;
import com.example.chat.websocket.WebSocketEventListenerManager;
import com.example.chat.websocket.WebSocketHandlerImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SessionMessageBrokerService
        implements WebSocketTextMessageReceiveListener,
                   DisposableBean {

    private static final Logger logger = LogManager.getLogger(SessionMessageBrokerService.class);

    private final SessionRepository sessionRepository;
    private final WebSocketEventListenerManager webSocketEventListenerManager;
    private final BlockingQueue<TaskWrapper> retryQueue;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;

    @Value("${message-broker.retry:5}")
    private int retryCount;

    public SessionMessageBrokerService(
            SessionRepository sessionRepository,
            WebSocketEventListenerManager webSocketEventListenerManager,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.webSocketEventListenerManager = webSocketEventListenerManager;
        this.objectMapper = objectMapper;
        this.retryQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newScheduledThreadPool(
                12,
                new CustomizableThreadFactory("Session Message Broker Service"));
        this.webSocketEventListenerManager.addListener(this);
        this.executorService.submit(this::exhaustTask);
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void sendMessage(String sessionId, Object message) {
        enqueueTask(() -> {
            if (!this.sessionRepository.containsEntityByKey(sessionId)) {
                return;
            }
            String payload = this.objectMapper.writeValueAsString(message);
            WebSocketSession session = this.sessionRepository
                    .findEntityByKey(sessionId)
                    .orElseThrow();
            session.sendMessage(new TextMessage(payload));
        });
    }

    public void sendMessage(String sessionId, String message) {
        enqueueTask(() -> {
            if (!this.sessionRepository.containsEntityByKey(sessionId)) {
                return;
            }
            WebSocketSession session = this.sessionRepository
                    .findEntityByKey(sessionId)
                    .orElseThrow();
            session.sendMessage(new TextMessage(message));
        });
    }

    @Override
    public void destroy() throws Exception {
        this.webSocketEventListenerManager.removeListener(this);
        this.executorService.shutdown();
    }

    @Override
    public void onTextMessageReceive(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        if (!this.sessionRepository.containsEntityByKey(sessionId)) {
            return;
        }
        String payload = message.getPayload();
        try {
            JsonNode jsonData = this.objectMapper.readTree(payload);
            WebSocketHandlerImpl webSocketHandler = (WebSocketHandlerImpl) this.webSocketEventListenerManager;
            for (WebSocketJsonMessageReceiveListener listener :
                    webSocketHandler.getListeners(WebSocketJsonMessageReceiveListener.class)) {
                listener.onJsonMessageReceive(session, jsonData);
            }
        } catch (JsonProcessingException e) {
            JsonNode errorMessageData = JsonNodeFactory.instance.objectNode()
                    .put("sessionId", sessionId)
                    .put("kind", "error")
                    .put("message", "invalid payload")
                    .put("timestamp", LocalDateTime
                            .now()
                            .format(DateTimeFormatter.ISO_DATE_TIME));
            sendMessage(sessionId, errorMessageData.toString());
        }
    }

    @PostConstruct
    private void postConstruct() {
        logger.info("Session Message Broker Service started with retry count: {}", this.retryCount);
    }

    private void enqueueTask(Task task) {
        this.retryQueue.add(new TaskWrapper(task));
    }

    private void exhaustTask() {
        try {
            TaskWrapper task;
            while (true) {
                task = this.retryQueue.take();
                logger.info("Task taken from queue: {}", task);
                try {
                    task.run();
                } catch (Exception e) {
                    if (task.getCounter() < this.retryCount) {
                        logger.warn("Failed to exhaust task {}. Retry the task", task);
                        this.retryQueue.add(task);
                    } else {
                        logger.error(e);
                    }
                }
            }
        } catch (InterruptedException e) {
            this.retryQueue.clear();
        }
    }

    @FunctionalInterface
    private static interface Task {

        public void run() throws Exception;

    }

    private static class TaskWrapper implements Task {

        private final Task task;
        private final AtomicInteger counter;

        private TaskWrapper(Task task) {
            this.task = task;
            counter = new AtomicInteger();
        }

        @Override
        public void run() throws Exception {
            this.task.run();
        }

        public int getCounter() {
            return counter.incrementAndGet();
        }

    }

}
