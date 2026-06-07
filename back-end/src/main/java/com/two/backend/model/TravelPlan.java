package com.two.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
/**
 * 旅行计划实体，包含计划基础信息、附件信息以及申请人数统计字段。
 */
public class TravelPlan {
    public static final int STATUS_APPLYING = 0;
    public static final int STATUS_FULL = 1;

    private Long id;
    private String planNo;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private Integer capacity;
    private Boolean published;
    private String filePath;
    private String fileName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer applicantTotal;
    private Integer myApplicantCount;
}
