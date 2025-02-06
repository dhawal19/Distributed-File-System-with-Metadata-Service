package com.dhawal.service;

import com.dhawal.common.Payload;
import com.dhawal.model.OutboxEvent;
import com.dhawal.repository.OutboxEventRepository;
import com.mongodb.MongoGridFSException;
import com.mongodb.MongoQueryException;
import com.mongodb.client.gridfs.model.GridFSFile;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
@Transactional
public class FileStorageService {
    private final GridFsTemplate gridFsTemplate;
    private final OutboxEventRepository outboxEventRepository;

    @Autowired
    public FileStorageService(GridFsTemplate gridFsTemplate, OutboxEventRepository outboxEventRepository){
        this.gridFsTemplate = gridFsTemplate;
        this.outboxEventRepository = outboxEventRepository;
    }

    public String uploadFile(MultipartFile file) throws IOException, MongoGridFSException, MongoQueryException {
        InputStream inputStream = file.getInputStream();
        // Optional metadata
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        org.bson.Document metadata = new org.bson.Document();
        metadata.put("contentType", fileType);
        metadata.put("filename", fileName);

        Object fileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType(), metadata);
        String fileIdString = fileId.toString();
        Payload payload = new Payload(fileIdString, fileName, fileType, file.getSize());

        OutboxEvent event = new OutboxEvent();
        saveOutboxEvent(event, "FileUpload", payload);
        return fileIdString;
    }

    public GridFsResource downloadFile(String fileId) throws FileNotFoundException {
        GridFSFile file = gridFsTemplate.findOne(new Query(where("_id").is(fileId)));
        return gridFsTemplate.getResource(file);
    }

    public void deleteFile(String fileId) throws Exception {
        gridFsTemplate.delete(new Query(where("_id").is(fileId)));

        OutboxEvent outboxEvent = new OutboxEvent();

        Payload payload = new Payload(fileId);

        saveOutboxEvent(outboxEvent, "FileDeleted", payload);
    }

    private void saveOutboxEvent(OutboxEvent event, String eventType, Payload payload) {
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setCreatedAt(LocalDateTime.now());
        event.setStatus("Pending");
        outboxEventRepository.save(event);
    }

}
