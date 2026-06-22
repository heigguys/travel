package com.two.backend.mapper;

import com.two.backend.model.Consultation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
/**
 * 咨询 Mapper，负责 consultations 表的查询、插入和关闭状态更新。
 */
public interface ConsultationMapper {
    /**
     * 查询计划下全部公开咨询消息。
     */
    List<Consultation> listByPlan(@Param("planId") Long planId);

    /**
     * 插入新的咨询消息，并回填自增 ID。
     */
    int insert(Consultation consultation);

    /**
     * 记录管理员已查看某个计划咨询消息。
     */
    int markAdminRead(@Param("planId") Long planId);

    /**
     * 记录普通用户已查看某个计划咨询消息。
     */
    int markUserRead(@Param("planId") Long planId, @Param("userId") Long userId);

    /**
     * 关闭指定用户在指定计划下的咨询会话。
     */
    int close(@Param("planId") Long planId, @Param("userId") Long userId);
}
