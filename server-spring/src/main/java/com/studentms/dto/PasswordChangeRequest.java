package com.studentms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank(message = "请填写原密码与新密码") String oldPassword,
    @NotBlank(message = "请填写原密码与新密码")
        @Size(min = 6, max = 64, message = "新密码长度需在 6～64 位")
        String newPassword) {}
