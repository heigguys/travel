package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Application {
    private Long id;
    private Long planId;
    private Long userId;
    private Integer applicantCount;
    private String optionText;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String planNo;
    private String destination;
    private String userName;
    private String employeeNo;
    private String email;
}
