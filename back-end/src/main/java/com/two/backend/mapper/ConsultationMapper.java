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
public interface ConsultationMapper {
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

    @Insert("""
            insert into consultations(plan_id, user_id, participant_user_id, sender_role, content, status)
            values(#{planId}, #{userId}, #{participantUserId}, #{senderRole}, #{content}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Consultation consultation);

    @Update("update consultations set status = 'CLOSED' where plan_id = #{planId} and participant_user_id = #{userId}")
    int close(@Param("planId") Long planId, @Param("userId") Long userId);
}
