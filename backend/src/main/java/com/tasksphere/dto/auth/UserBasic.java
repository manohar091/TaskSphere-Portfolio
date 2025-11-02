package com.tasksphere.dto.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBasic {
    private Long id;
    private String name;
    private String email;
    private List<String> roles;
}