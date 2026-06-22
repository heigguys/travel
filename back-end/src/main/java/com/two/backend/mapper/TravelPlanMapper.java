package com.two.backend.mapper;

import com.two.backend.model.TravelPlan;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
/**
 * 旅行计划 Mapper，负责 travel_plans 表的查询、插入、更新和删除。
 * status 字段存储计算后的整数状态：0=可申请，1=已成团，2=进行中，3=已结束，4=未成团。
 */
public interface TravelPlanMapper {
    /**
     * 按角色、关键字、状态和排序条件查询计划，并计算申请人数统计字段。
     */
    List<TravelPlan> list(@Param("admin") boolean admin, @Param("userId") Long userId, @Param("keyword") String keyword,
                          @Param("status") Integer status, @Param("published") Boolean published,
                          @Param("sort") String sort, @Param("sortDir") String sortDir);

    /**
     * 根据 ID 查询旅行计划。
     */
    TravelPlan findById(@Param("id") Long id);

    /**
     * 插入旅行计划，并回填自增 ID。
     */
    int insert(TravelPlan plan);

    /**
     * 更新旅行计划基础信息、附件信息和更新时间。
     */
    int update(TravelPlan plan);

    /**
     * 更新单条旅行计划的状态（申请或取消时触发）。
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 批量刷新所有旅行计划的状态，根据日期和当前申请人数重新计算。
     * 登录时和每天凌晨自动触发。
     */
    int updateAllStatuses();

    /**
     * 删除指定旅行计划。
     */
    int delete(@Param("id") Long id);
}
