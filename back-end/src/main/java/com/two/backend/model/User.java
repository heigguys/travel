package com.two.backend.model;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String employeeNo;
    private String name;
    private String email;
    private Role role;
    private String passwordMd5;
    private Boolean enabled;
}
