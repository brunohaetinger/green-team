package com.greenteam.util;

import com.fasterxml.jackson.databind.JsonNode;

/*
    * This class contains utility methods for working with JSON data in the enrichment job.
*/
public final class JsonUtils {

    private JsonUtils() {}

    // The resolvePayload method is responsible for extracting the relevant JSON node from the raw JSON message that is consumed from Kafka.
    // The raw JSON messages are expected to have a structure that may contain a "payload" field, which in turn may contain an "after" field that contains the actual data we want to extract. 
    // This method checks for the presence of these fields and returns the appropriate JSON node that contains the data we want to work with in the enrichment process. 
    // If the expected fields are not present in the JSON message, it returns the root JSON node, which allows the parsing operators to handle the message and potentially fail with a deserialization error if the required fields are missing.
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

    // The requiredText method is a helper method that extracts a required text field from a JSON node.
    // It checks if the specified field is present in the JSON node and if it contains a non-blank text value. 
    // If the field is missing, null, or blank, it throws an IllegalArgumentException, 
    // which indicates that the JSON message is malformed and cannot be processed further in the enrichment job.
    public static String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.asText();
    }

    // The requiredInt method is a helper method that extracts a required integer field from a JSON node.
    // It checks if the specified field is present in the JSON node and if it contains a
    // valid integer value. If the field is missing, null, or cannot be parsed as an integer, it throws an IllegalArgumentException,
    // which indicates that the JSON message is malformed and cannot be processed further in the enrichment job.
    public static int requiredInt(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.asInt(Integer.MIN_VALUE);
    }

    // The normalizeCountry method is a helper method that takes a country name as input and normalizes it to a standard format.
    // This method is used to ensure that the country names in the store events are consistent and can be easily compared and analyzed in the enrichment process. 
    // For example, it converts common abbreviations for Brazil ("BR", "BRA") to the full country name "Brazil".
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