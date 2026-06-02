package com.two.backend.mapper;

import com.two.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
/**
 * 用户 Mapper，负责 users 表的登录查询、会话查询和密码更新。
 */
public interface UserMapper {
    /**
     * 按员工编号查询启用用户。
     */
    @Select("select * from users where employee_no = #{employeeNo} and enabled = true")
    User findByEmployeeNo(String employeeNo);

    /**
     * 按用户 ID 查询启用用户。
     */
    @Select("select * from users where id = #{id} and enabled = true")
    User findById(Long id);

    /**
     * 更新用户密码 MD5 摘要。
     */
    @Update("update users set password_md5 = #{passwordMd5} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordMd5") String passwordMd5);
}
