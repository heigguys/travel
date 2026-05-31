package com.two.backend.mapper;

import com.two.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("select * from users where employee_no = #{employeeNo} and enabled = true")
    User findByEmployeeNo(String employeeNo);

    @Select("select * from users where id = #{id} and enabled = true")
    User findById(Long id);

    @Update("update users set password_md5 = #{passwordMd5} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordMd5") String passwordMd5);
}
