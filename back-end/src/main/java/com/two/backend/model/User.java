package com.two.backend.model;

import lombok.Data;

@Data
/**
 * 系统用户实体，表示管理员或普通员工账号。
 */
public class User {
    private Long id;
    private String employeeNo;
    private String name;
    private String email;
    private Role role;
    private String passwordMd5;
    private Boolean enabled;
}
