package com.two.backend.mapper;

import com.two.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
/**
 * 用户 Mapper，负责 users 表的登录查询、会话查询和密码更新。
 */
public interface UserMapper {
    /**
     * 按员工编号查询启用用户。
     */
    User findByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 按用户 ID 查询启用用户。
     */
    User findById(@Param("id") Long id);

    /**
     * 更新用户密码 MD5 摘要。
     */
    int updatePassword(@Param("id") Long id, @Param("passwordMd5") String passwordMd5);
}
