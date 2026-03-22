package com.greenteam.webserverdatacollector.service;

import com.greenteam.webserverdatacollector.entity.IngestionOffset;
import com.greenteam.webserverdatacollector.repository.IngestionOffsetRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class OffsetService {

    private final IngestionOffsetRepository repository;

    public OffsetService(IngestionOffsetRepository repository) {
        this.repository = repository;
    }

    public Long getOffset(String source) {
        return repository.findById(source)
                .map(IngestionOffset::getLastProcessedId)
                .orElse(0L);
    }

    public void updateOffset(String source, Long newOffset) {
        IngestionOffset offset = repository.findById(source)
                .orElseGet(() -> {
                    IngestionOffset o = new IngestionOffset();
                    o.setSourceName(source);
                    return o;
                });

        offset.setLastProcessedId(newOffset);
        offset.setUpdatedAt(LocalDateTime.now());

        repository.save(offset);
    }
}