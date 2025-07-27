package com.krainet.auth.dto;

import com.krainet.auth.entity.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
