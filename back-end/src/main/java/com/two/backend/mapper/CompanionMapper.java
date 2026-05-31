package com.two.backend.mapper;

import com.two.backend.model.Companion;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompanionMapper {
    @Select("select * from companions where application_id = #{applicationId} order by id")
    List<Companion> listByApplication(Long applicationId);

    @Insert("""
            insert into companions(application_id, name, gender, id_card, bed_needed)
            values(#{applicationId}, #{name}, #{gender}, #{idCard}, #{bedNeeded})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Companion companion);

    @Delete("delete from companions where application_id = #{applicationId}")
    int deleteByApplication(Long applicationId);
}
