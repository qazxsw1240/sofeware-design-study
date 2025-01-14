package com.example.chat.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonNodeUtils {

    public static JsonNode createErrorMessageData(String sessionId, String message, LocalDateTime time) {
        return JsonNodeFactory.instance.objectNode()
                .put("sessionId", sessionId)
                .put("kind", "error")
                .put("message", message)
                .put("timestamp", time.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public static JsonNode crateErrorMessageData(String sessionId, String message) {
        return createErrorMessageData(sessionId, message, LocalDateTime.now());
    }

}
