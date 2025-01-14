package com.example.chat.service.websocket;

import com.example.chat.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SessionMessageBrokerService implements DisposableBean {

    private static final Logger logger = LogManager.getLogger(SessionMessageBrokerService.class);

    private final SessionRepository sessionRepository;
    private final BlockingQueue<TaskWrapper> retryQueue;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;

    @Value("${message-broker.retry:5}")
    private int retryCount;

    public SessionMessageBrokerService(
            SessionRepository sessionRepository,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.retryQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newScheduledThreadPool(
                12,
                new CustomizableThreadFactory("Session Message Broker Service"));
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
        this.executorService.shutdown();
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
