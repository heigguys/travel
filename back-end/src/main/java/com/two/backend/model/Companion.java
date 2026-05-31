package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Companion {
    private Long id;
    private Long applicationId;
    private String name;
    private String gender;
    private String idCard;
    private Boolean bedNeeded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
