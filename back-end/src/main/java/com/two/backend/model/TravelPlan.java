package com.two.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TravelPlan {
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
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer applicantTotal;
    private Integer myApplicantCount;
}
