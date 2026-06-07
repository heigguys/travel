package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
/**
 * 旅行申请实体，包含表字段以及查询列表时补充的计划和用户展示字段。
 * status 字段用整数存储：0 = 有效，1 = 已取消。
 */
public class Application {
    public static final int STATUS_ACTIVE   = 0;
    public static final int STATUS_CANCELED = 1;

    private Long id;
    private Long planId;
    private Long userId;
    private Integer applicantCount;
    private String optionText;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String planNo;
    private String destination;
    private String userName;
    private String employeeNo;
    private String email;
}
