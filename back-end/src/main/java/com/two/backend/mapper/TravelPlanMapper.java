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
/**
 * 旅行计划 Mapper，负责 travel_plans 表的查询、插入、更新和删除。
 * status 字段存储计算后的整数状态：0=可申请，1=已成团，2=进行中，3=已结束，4=未成团。
 */
public interface TravelPlanMapper {
    /**
     * 按角色、关键字、状态和排序条件查询计划，并计算申请人数统计字段。
     */
    @Select("""
            <script>
            select p.*,
              coalesce((select sum(a.applicant_count) from applications a where a.plan_id = p.id and a.status = 0), 0) applicant_total,
              coalesce((select a.applicant_count from applications a where a.plan_id = p.id and a.user_id = #{userId} and a.status = 0), 0) my_applicant_count,
              case
                when #{admin} = true and exists (
                  select 1
                  from consultations c
                  left join consultation_admin_reads r on r.plan_id = c.plan_id
                  where c.plan_id = p.id
                    and c.sender_role = 1
                    and c.created_at > coalesce(r.last_read_at, '1970-01-01 00:00:00')
                ) then true
                when #{admin} = false and exists (
                  select 1
                  from consultations c
                  left join consultation_user_reads r on r.plan_id = c.plan_id and r.user_id = #{userId}
                  where c.plan_id = p.id
                    and c.user_id != #{userId}
                    and c.created_at > coalesce(r.last_read_at, '1970-01-01 00:00:00')
                ) then true
                else false
              end has_unread_consultation
            from travel_plans p
            where 1 = 1
              <if test="admin == false">and p.published = true</if>
              <if test="status != null">and p.status = #{status}</if>
              <if test="keyword != null and keyword != ''">
                and (p.destination like concat('%', #{keyword}, '%') or p.plan_no like concat('%', #{keyword}, '%'))
              </if>
            order by
              <choose>
                <when test="sort == 'planNo'">p.plan_no</when>
                <when test="sort == 'destination'">p.destination</when>
                <when test="sort == 'startDate'">p.start_date</when>
                <when test="sort == 'price'">p.price</when>
                <when test="sort == 'capacity'">p.capacity</when>
                <when test="sort == 'applicantTotal'">applicant_total</when>
                <when test="sort == 'myApplicantCount'">my_applicant_count</when>
                <otherwise>p.created_at</otherwise>
              </choose>
              <choose>
                <when test="sortDir == 'asc'">asc</when>
                <otherwise>desc</otherwise>
              </choose>
            </script>
            """)
    List<TravelPlan> list(@Param("admin") boolean admin, @Param("userId") Long userId, @Param("keyword") String keyword,
                          @Param("status") Integer status, @Param("sort") String sort, @Param("sortDir") String sortDir);

    /**
     * 根据 ID 查询旅行计划。
     */
    @Select("select * from travel_plans where id = #{id}")
    TravelPlan findById(Long id);

    /**
     * 插入旅行计划，并回填自增 ID。
     */
    @Insert("""
            insert into travel_plans(plan_no, destination, start_date, end_date, price, capacity, published, file_path, file_name, status)
            values(#{planNo}, #{destination}, #{startDate}, #{endDate}, #{price}, #{capacity}, #{published}, #{filePath}, #{fileName}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TravelPlan plan);

    /**
     * 更新旅行计划基础信息、附件信息和更新时间。
     */
    @Update("""
            update travel_plans
            set destination = #{destination}, start_date = #{startDate}, end_date = #{endDate}, price = #{price},
                capacity = #{capacity}, published = #{published}, file_path = #{filePath}, file_name = #{fileName},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int update(TravelPlan plan);

    /**
     * 更新单条旅行计划的状态（申请或取消时触发）。
     */
    @Update("update travel_plans set status = #{status}, updated_at = current_timestamp where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 批量刷新所有旅行计划的状态，根据日期和当前申请人数重新计算。
     * 登录时和每天凌晨自动触发。
     */
    @Update("""
            update travel_plans p
            left join (
              select plan_id, coalesce(sum(applicant_count), 0) as total
              from applications
              where status = 0
              group by plan_id
            ) agg on agg.plan_id = p.id
            set p.status = case
              when p.end_date < current_date and coalesce(agg.total, 0) >= p.capacity then 3
              when p.end_date < current_date then 4
              when p.start_date <= current_date and coalesce(agg.total, 0) >= p.capacity then 2
              when p.start_date <= current_date then 4
              when coalesce(agg.total, 0) >= p.capacity then 1
              else 0
            end,
            p.updated_at = current_timestamp
            """)
    int updateAllStatuses();

    /**
     * 删除指定旅行计划。
     */
    @Delete("delete from travel_plans where id = #{id}")
    int delete(Long id);
}
