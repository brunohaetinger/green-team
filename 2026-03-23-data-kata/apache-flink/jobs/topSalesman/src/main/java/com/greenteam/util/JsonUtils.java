package com.greenteam.util;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonUtils {

    private JsonUtils() {}

    public static String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.asText();
    }

    public static String optionalText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.asText().isBlank()) {
            return null;
        }
        return value.asText();
    }
}