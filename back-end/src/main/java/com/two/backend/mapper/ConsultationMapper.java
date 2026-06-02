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
     * 查询计划咨询；管理员可看全部，普通用户只看自己的会话。
     */
    @Select("""
            select c.*, u.name user_name, u.employee_no, p.destination
            from consultations c
            join users u on u.id = c.user_id
            join travel_plans p on p.id = c.plan_id
            where c.plan_id = #{planId}
              and (#{admin} = true or c.participant_user_id = #{userId})
            order by c.created_at
            """)
    List<Consultation> listByPlan(@Param("planId") Long planId, @Param("userId") Long userId, @Param("admin") boolean admin);

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
     * 关闭指定用户在指定计划下的咨询会话。
     */
    @Update("update consultations set status = 'CLOSED' where plan_id = #{planId} and participant_user_id = #{userId}")
    int close(@Param("planId") Long planId, @Param("userId") Long userId);
}
