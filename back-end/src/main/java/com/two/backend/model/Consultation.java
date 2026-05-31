package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Consultation {
    private Long id;
    private Long planId;
    private Long userId;
    private Long participantUserId;
    private String senderRole;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private String userName;
    private String employeeNo;
    private String destination;
}
