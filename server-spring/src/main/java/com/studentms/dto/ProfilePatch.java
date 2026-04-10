package com.studentms.dto;

import jakarta.validation.constraints.Size;

public record ProfilePatch(
    @Size(max = 128, message = "邮箱过长") String email,
    @Size(max = 32, message = "手机号过长") String phone,
    @Size(max = 512, message = "头像链接过长") String avatarUrl) {}
