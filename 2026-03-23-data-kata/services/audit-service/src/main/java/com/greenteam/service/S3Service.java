package com.greenteam.service;

import com.greenteam.config.S3Config;
import com.greenteam.model.SalesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Config s3Config;

    public S3Service(S3Client s3Client, S3Config s3Config) {
        this.s3Client = s3Client;
        this.s3Config = s3Config;
    }

    public void saveEvent(SalesEvent event) {
        String key = buildKey(event);
        String bucketName = s3Config.getBucketName();

        try {
            String existingContent = getExistingContent(bucketName, key);
            String newContent;

            if (existingContent == null) {
                newContent = SalesEvent.getCsvHeader() + System.lineSeparator() + event.toCsvRow() + System.lineSeparator();
            } else {
                newContent = existingContent + event.toCsvRow() + System.lineSeparator();
            }

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/csv")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(newContent, StandardCharsets.UTF_8));

            logger.info("Saved event to s3://{}/{}", bucketName, key);
        } catch (Exception e) {
            logger.error("Failed to save event to s3://{}/{}: {}", bucketName, key, e.getMessage(), e);
            throw new RuntimeException("Failed to save event to S3", e);
        }
    }

    private String getExistingContent(String bucketName, String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(getRequest).asUtf8String();
        } catch (NoSuchKeyException e) {
            return null;
        } catch (Exception e) {
            logger.warn("Error reading existing file s3://{}/{}: {}", bucketName, key, e.getMessage());
            return null;
        }
    }

    private String buildKey(SalesEvent event) {
        String dateOnly = extractDateOnly(event.getSaleDate());
        String countryName = sanitizePathComponent(event.getCountryName());
        String cityName = sanitizePathComponent(event.getCityName());
        int saleId = event.getSaleId();

        return String.format("%s/%s/%s/%d.csv", dateOnly, countryName, cityName, saleId);
    }

    private String extractDateOnly(String saleDate) {
        if (saleDate == null || saleDate.isEmpty()) {
            return "unknown-date";
        }
        if (saleDate.contains("T")) {
            return saleDate.substring(0, saleDate.indexOf("T"));
        }
        if (saleDate.length() >= 10) {
            return saleDate.substring(0, 10);
        }
        return saleDate;
    }

    private String sanitizePathComponent(String value) {
        if (value == null || value.isEmpty()) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9\\-_\\s]", "").trim();
    }
}
