package com.two.backend.mapper;

import com.two.backend.model.TravelPlan;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TravelPlanMapper {
    @Select("""
            <script>
            select p.*,
              coalesce((select sum(a.applicant_count) from applications a where a.plan_id = p.id and a.status = 'ACTIVE'), 0) applicant_total,
              coalesce((select a.applicant_count from applications a where a.plan_id = p.id and a.user_id = #{userId} and a.status = 'ACTIVE'), 0) my_applicant_count
            from travel_plans p
            where 1 = 1
              <if test="admin == false">and p.published = true</if>
              <if test="status != null and status != ''">and p.status = #{status}</if>
              <if test="keyword != null and keyword != ''">
                and (p.destination like concat('%', #{keyword}, '%') or p.plan_no like concat('%', #{keyword}, '%'))
              </if>
            order by
              <choose>
                <when test="sort == 'price'">p.price</when>
                <when test="sort == 'startDate'">p.start_date</when>
                <when test="sort == 'capacity'">p.capacity</when>
                <otherwise>p.created_at</otherwise>
              </choose> desc
            </script>
            """)
    List<TravelPlan> list(@Param("admin") boolean admin, @Param("userId") Long userId, @Param("keyword") String keyword,
                          @Param("status") String status, @Param("sort") String sort);

    @Select("select * from travel_plans where id = #{id}")
    TravelPlan findById(Long id);

    @Insert("""
            insert into travel_plans(plan_no, destination, start_date, end_date, price, capacity, published, file_path, file_name, status)
            values(#{planNo}, #{destination}, #{startDate}, #{endDate}, #{price}, #{capacity}, #{published}, #{filePath}, #{fileName}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TravelPlan plan);

    @Update("""
            update travel_plans
            set destination = #{destination}, start_date = #{startDate}, end_date = #{endDate}, price = #{price},
                capacity = #{capacity}, published = #{published}, file_path = #{filePath}, file_name = #{fileName},
                status = #{status}, updated_at = current_timestamp
            where id = #{id}
            """)
    int update(TravelPlan plan);

    @Delete("delete from travel_plans where id = #{id}")
    int delete(Long id);
}
