package com.greenteam.util;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonUtils {

    private JsonUtils() {}

    public static JsonNode resolvePayload(JsonNode root) {
        if (root == null || root.isNull()) {
            throw new IllegalArgumentException("message is null");
        }

        JsonNode payload = root.get("payload");
        if (payload != null && payload.isObject()) {
            JsonNode after = payload.get("after");
            if (after != null && after.isObject()) {
                return after;
            }
            return payload;
        }

        return root;
    }

    public static String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.asText();
    }

    public static int requiredInt(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.asInt(Integer.MIN_VALUE);
    }

    public static String normalizeCountry(String country) {
        if (country == null) {
            return null;
        }

        String normalized = country.trim();
        if (normalized.equalsIgnoreCase("BR") || normalized.equalsIgnoreCase("BRA")) {
            return "Brazil";
        }

        return normalized;
    }
}