package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
/**
 * 旅行申请实体，包含表字段以及查询列表时补充的计划和用户展示字段。
 */
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
