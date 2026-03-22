package com.greenteam.webserverdatacollector.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class IngestionOffset {

    @Id
    private String sourceName;
    private Long lastProcessedId;
    private LocalDateTime updatedAt;

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Long getLastProcessedId() {
        return lastProcessedId;
    }

    public void setLastProcessedId(Long lastProcessedId) {
        this.lastProcessedId = lastProcessedId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
