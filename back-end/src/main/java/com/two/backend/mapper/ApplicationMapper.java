package com.two.backend.mapper;

import com.two.backend.model.Application;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
/**
 * 旅行申请 Mapper，负责 applications 表的查询、插入和状态更新。
 */
public interface ApplicationMapper {
    /**
     * 查询指定用户对指定计划的有效申请。
     */
    @Select("select * from applications where plan_id = #{planId} and user_id = #{userId} and status = 'ACTIVE'")
    Application findActive(@Param("planId") Long planId, @Param("userId") Long userId);

    /**
     * 统计指定计划当前有效申请的总人数。
     */
    @Select("select coalesce(sum(applicant_count), 0) from applications where plan_id = #{planId} and status = 'ACTIVE'")
    int activeCount(Long planId);

    /**
     * 查询指定计划下的有效申请，并带出申请人和计划展示字段。
     */
    @Select("""
            select a.*, u.name user_name, u.employee_no, u.email, p.plan_no, p.destination
            from applications a
            join users u on u.id = a.user_id
            join travel_plans p on p.id = a.plan_id
            where a.plan_id = #{planId} and a.status = 'ACTIVE'
            order by a.created_at
            """)
    List<Application> listActiveByPlan(Long planId);

    /**
     * 查询指定用户的所有申请，并带出计划展示字段。
     */
    @Select("""
            select a.*, p.plan_no, p.destination
            from applications a
            join travel_plans p on p.id = a.plan_id
            where a.user_id = #{userId}
            order by a.updated_at desc
            """)
    List<Application> listByUser(Long userId);

    /**
     * 根据申请 ID 查询申请记录。
     */
    @Select("select * from applications where id = #{id}")
    Application findById(Long id);

    /**
     * 插入新的申请记录，并回填自增 ID。
     */
    @Insert("""
            insert into applications(plan_id, user_id, applicant_count, option_text, status)
            values(#{planId}, #{userId}, #{applicantCount}, #{optionText}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Application application);

    /**
     * 更新申请人数、备注、状态和更新时间。
     */
    @Update("""
            update applications
            set applicant_count = #{applicantCount}, option_text = #{optionText}, status = #{status}, updated_at = current_timestamp
            where id = #{id}
            """)
    int update(Application application);

    /**
     * 将申请状态标记为已取消。
     */
    @Update("update applications set status = 'CANCELED', updated_at = current_timestamp where id = #{id}")
    int cancel(Long id);

    /**
     * 删除指定计划下的申请记录。
     */
    @Delete("delete from applications where plan_id = #{planId}")
    int deleteByPlan(Long planId);
}
