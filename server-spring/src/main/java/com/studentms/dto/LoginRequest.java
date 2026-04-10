package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "请输入用户名和密码") String username,
    @NotBlank(message = "请输入用户名和密码") String password) {}
