package com.dhawal.repository;

import com.dhawal.model.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
    List<OutboxEvent> findByPublishedAtIsNullOrStatusEquals(String status);
}
