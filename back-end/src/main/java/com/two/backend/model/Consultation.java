package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
/**
 * 旅行咨询实体，记录员工和管理员围绕某个旅行计划的沟通内容。
 */
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
