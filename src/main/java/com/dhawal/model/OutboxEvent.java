package com.dhawal.model;

import com.dhawal.common.Payload;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
@Getter
@Setter
@Table(name = "outbox_event")
public class OutboxEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 100000000092L;
    @Id
    private String id;

    private String eventType;

    private Payload payload;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    private String status;
}
