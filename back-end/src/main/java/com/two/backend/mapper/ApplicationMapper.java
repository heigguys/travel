package com.two.backend.mapper;

import com.two.backend.model.Application;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
/**
 * 旅行申请 Mapper，负责 applications 表的查询、插入和状态更新。
 */
public interface ApplicationMapper {
    /**
     * 查询指定用户对指定计划的有效申请。
     */
    Application findActive(@Param("planId") Long planId, @Param("userId") Long userId);

    /**
     * 统计指定计划当前有效申请的总人数。
     */
    int activeCount(@Param("planId") Long planId);

    /**
     * 查询指定计划下的有效申请，并带出申请人和计划展示字段。
     */
    List<Application> listActiveByPlan(@Param("planId") Long planId);

    /**
     * 查询指定用户的所有申请，并带出计划展示字段。
     */
    List<Application> listByUser(@Param("userId") Long userId);

    /**
     * 根据申请 ID 查询申请记录。
     */
    Application findById(@Param("id") Long id);

    /**
     * 插入新的申请记录，并回填自增 ID。
     */
    int insert(Application application);

    /**
     * 更新申请人数、备注、状态和更新时间。
     */
    int update(Application application);

    /**
     * 将申请状态标记为已取消。
     */
    int cancel(@Param("id") Long id);

    /**
     * 删除指定计划下的申请记录。
     */
    int deleteByPlan(@Param("planId") Long planId);
}
