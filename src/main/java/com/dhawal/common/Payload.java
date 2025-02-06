package com.dhawal.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class Payload implements Serializable {
    @Serial
    private static final long serialVersionUID = 10000000009L;
    private String fileId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String[] filePermissions;

    public Payload(){}
    public Payload(String fileId, String fileName, String fileType, long fileSize){
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Payload(String fileId){
        this.fileId = fileId;
        this.fileName = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
