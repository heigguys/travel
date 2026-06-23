package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConsultationSession {
    private Long participantUserId;
    private String employeeNo;
    private String userName;
    private LocalDateTime latestCreatedAt;
    private Integer unreadCount;
}
