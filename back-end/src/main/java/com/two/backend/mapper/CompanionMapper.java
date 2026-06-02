package com.two.backend.mapper;

import com.two.backend.model.Companion;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
/**
 * 随行人员 Mapper，负责 companions 表的查询、插入和删除。
 */
public interface CompanionMapper {
    /**
     * 查询某条申请下的随行人员。
     */
    @Select("select * from companions where application_id = #{applicationId} order by id")
    List<Companion> listByApplication(Long applicationId);

    /**
     * 插入随行人员记录，并回填自增 ID。
     */
    @Insert("""
            insert into companions(application_id, name, gender, id_card, bed_needed)
            values(#{applicationId}, #{name}, #{gender}, #{idCard}, #{bedNeeded})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Companion companion);

    /**
     * 删除某条申请下的全部随行人员。
     */
    @Delete("delete from companions where application_id = #{applicationId}")
    int deleteByApplication(Long applicationId);
}
