package com.dhawal.controller;

import com.dhawal.service.FileStorageService;
import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/files")
@Slf4j
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam String fileId) {
        try {
            GridFsResource file = fileStorageService.downloadFile(fileId);
            if (file != null) {
                return ResponseEntity.ok()
                        .header("Content-Type", "application/octet-stream")
                        .body(file);
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
        } catch (MongoException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: File could not be retrieved.");
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = fileStorageService.uploadFile(file);
            if (fileId != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(fileId);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid file upload request.");
    }


    @DeleteMapping
    public ResponseEntity<?> deleteFile(@RequestParam String fileId) {
        try {
            fileStorageService.deleteFile(fileId);
            return ResponseEntity.noContent().build();
        } catch (FileNotFoundException e) {
            log.error("File {} not found: {}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (MongoException e) {
            log.error("Database error while deleting file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error occurred while deleting the file.");
        } catch (DataAccessException e) {
            log.error("Failed to save outbox event for file deletion {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving deletion event. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error while deleting file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please contact support.");
        }
    }

}
