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
public interface ApplicationMapper {
    @Select("select * from applications where plan_id = #{planId} and user_id = #{userId} and status = 'ACTIVE'")
    Application findActive(@Param("planId") Long planId, @Param("userId") Long userId);

    @Select("select coalesce(sum(applicant_count), 0) from applications where plan_id = #{planId} and status = 'ACTIVE'")
    int activeCount(Long planId);

    @Select("""
            select a.*, u.name user_name, u.employee_no, u.email, p.plan_no, p.destination
            from applications a
            join users u on u.id = a.user_id
            join travel_plans p on p.id = a.plan_id
            where a.plan_id = #{planId} and a.status = 'ACTIVE'
            order by a.created_at
            """)
    List<Application> listActiveByPlan(Long planId);

    @Select("""
            select a.*, p.plan_no, p.destination
            from applications a
            join travel_plans p on p.id = a.plan_id
            where a.user_id = #{userId}
            order by a.updated_at desc
            """)
    List<Application> listByUser(Long userId);

    @Select("select * from applications where id = #{id}")
    Application findById(Long id);

    @Insert("""
            insert into applications(plan_id, user_id, applicant_count, option_text, status)
            values(#{planId}, #{userId}, #{applicantCount}, #{optionText}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Application application);

    @Update("""
            update applications
            set applicant_count = #{applicantCount}, option_text = #{optionText}, status = #{status}, updated_at = current_timestamp
            where id = #{id}
            """)
    int update(Application application);

    @Update("update applications set status = 'CANCELED', updated_at = current_timestamp where id = #{id}")
    int cancel(Long id);

    @Delete("delete from applications where plan_id = #{planId}")
    int deleteByPlan(Long planId);
}
