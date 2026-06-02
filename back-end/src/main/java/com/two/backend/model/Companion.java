package com.two.backend.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
/**
 * 随行人员实体，记录某个旅行申请下的同行人资料。
 */
public class Companion {
    private Long id;
    private Long applicationId;
    private String name;
    private String gender;
    private String idCard;
    private Boolean bedNeeded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
