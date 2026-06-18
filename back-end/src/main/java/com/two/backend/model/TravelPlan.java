package com.two.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
/**
 * 旅行计划实体，包含计划基础信息、附件信息以及申请人数统计字段。
 * status 为查询时动态计算的整数：0=可申请，1=已成团，2=进行中，3=已结束，4=未成团。
 */
public class TravelPlan {
    public static final int STATUS_AVAILABLE    = 0; // 可申请：未出发且未满员
    public static final int STATUS_FORMED       = 1; // 已成团：未出发且已满员
    public static final int STATUS_IN_PROGRESS  = 2; // 进行中：行程中
    public static final int STATUS_ENDED        = 3; // 已结束：结束且满员出行
    public static final int STATUS_DISBANDED    = 4; // 未成团：结束时人数不足

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
    private Boolean hasUnreadConsultation;
}
