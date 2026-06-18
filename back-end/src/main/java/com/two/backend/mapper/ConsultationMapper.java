package com.two.backend.mapper;

import com.two.backend.model.Consultation;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
/**
 * 咨询 Mapper，负责 consultations 表的查询、插入和关闭状态更新。
 */
public interface ConsultationMapper {
    /**
     * 查询计划下全部公开咨询消息。
     */
    @Select("""
            select c.*, u.name user_name, u.employee_no, p.destination
            from consultations c
            join users u on u.id = c.user_id
            join travel_plans p on p.id = c.plan_id
            where c.plan_id = #{planId}
            order by c.created_at, c.id
            """)
    List<Consultation> listByPlan(@Param("planId") Long planId);

    /**
     * 插入新的咨询消息，并回填自增 ID。
     */
    @Insert("""
            insert into consultations(plan_id, user_id, participant_user_id, sender_role, content, status)
            values(#{planId}, #{userId}, #{participantUserId}, #{senderRole}, #{content}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Consultation consultation);

    /**
     * 记录管理员已查看某个计划咨询消息。
     */
    @Insert("""
            insert into consultation_admin_reads(plan_id, last_read_at)
            values(#{planId}, current_timestamp)
            on duplicate key update last_read_at = values(last_read_at)
            """)
    int markAdminRead(@Param("planId") Long planId);

    /**
     * 记录普通用户已查看某个计划咨询消息。
     */
    @Insert("""
            insert into consultation_user_reads(plan_id, user_id, last_read_at)
            values(#{planId}, #{userId}, current_timestamp)
            on duplicate key update last_read_at = values(last_read_at)
            """)
    int markUserRead(@Param("planId") Long planId, @Param("userId") Long userId);

    /**
     * 关闭指定用户在指定计划下的咨询会话。
     */
    @Update("update consultations set status = 'CLOSED' where plan_id = #{planId} and participant_user_id = #{userId}")
    int close(@Param("planId") Long planId, @Param("userId") Long userId);
}
