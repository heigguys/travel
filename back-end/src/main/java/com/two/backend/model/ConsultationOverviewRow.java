package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConsultationOverviewRow {
    private Long participantUserId;
    private String employeeNo;
    private String userName;
    private Long planId;
    private String planNo;
    private String destination;
    private LocalDateTime latestCreatedAt;
    private Integer unreadCount;
}
