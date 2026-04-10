package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.LoginRequest;
import com.studentms.dto.LoginResponse;
import com.studentms.dto.PasswordChangeRequest;
import com.studentms.dto.ProfilePatch;
import com.studentms.dto.UserPayload;
import com.studentms.entity.UserEntity;
import com.studentms.repo.UserRepository;
import com.studentms.security.AppUser;
import com.studentms.security.JwtService;
import com.studentms.service.AuditService;
import com.studentms.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.regex.Pattern;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private static final Pattern EMAIL =
      Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PHONE = Pattern.compile("^(\\+?\\d{6,20})$");

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuditService auditService;
  private final LoginAttemptService loginAttemptService;

  public AuthController(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuditService auditService,
      LoginAttemptService loginAttemptService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.auditService = auditService;
    this.loginAttemptService = loginAttemptService;
  }

  @PostMapping("/api/auth/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest req) {
    String username = body.username().trim();
    loginAttemptService.checkAllowed(username);

    UserEntity u = userRepository.findByUsername(username).orElse(null);
    if (u == null || !passwordEncoder.matches(body.password(), u.getPasswordHash())) {
      loginAttemptService.onFailure(username);
      throw ApiException.badRequest("用户名或密码错误");
    }
    loginAttemptService.onSuccess(username);

    String token = jwtService.createToken(u);
    UserPayload userPayload = UserPayload.from(u);
    AppUser au =
        new AppUser(u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getStudentId());
    auditService.insert(au, req, "登录系统", "用户" + username + "登录系统");
    return ApiResponse.ok(new LoginResponse(token, userPayload));
  }

  @PostMapping("/api/auth/logout")
  public ApiResponse<Void> logout() {
    return ApiResponse.ok(null);
  }

  @GetMapping("/api/auth/me")
  public ApiResponse<UserPayload> me(@AuthenticationPrincipal AppUser principal) {
    UserEntity u =
        userRepository
            .findById(principal.id())
            .orElseThrow(() -> ApiException.unauthorized("未登录或登录已失效"));
    return ApiResponse.ok(UserPayload.from(u));
  }

  @PatchMapping("/api/auth/profile")
  public ApiResponse<UserPayload> patchProfile(
      @Valid @RequestBody ProfilePatch body,
      @AuthenticationPrincipal AppUser principal,
      HttpServletRequest req) {
    UserEntity u =
        userRepository
            .findById(principal.id())
            .orElseThrow(() -> ApiException.unauthorized("未登录或登录已失效"));

    String email = normOpt(body.email());
    String phone = normOpt(body.phone());
    String avatarUrl = normOpt(body.avatarUrl());

    if (email != null && !EMAIL.matcher(email).matches()) {
      throw ApiException.badRequest("邮箱格式不正确");
    }
    if (phone != null && !PHONE.matcher(phone).matches()) {
      throw ApiException.badRequest("手机号格式不正确");
    }
    if (avatarUrl != null) {
      if (avatarUrl.length() > 512) {
        throw ApiException.badRequest("头像链接过长");
      }
      String lower = avatarUrl.toLowerCase();
      boolean okHttp = lower.startsWith("https://") || lower.startsWith("http://");
      boolean okUpload =
          lower.startsWith("/uploads/avatars/")
              && !lower.contains("..")
              && principal.id() != null
              && lower.startsWith("/uploads/avatars/" + principal.id() + ".");
      if (!okHttp && !okUpload) {
        throw ApiException.badRequest("头像需为 http(s) 链接或本系统上传的头像路径");
      }
    }

    u.setEmail(email);
    u.setPhone(phone);
    u.setAvatarUrl(avatarUrl);
    userRepository.save(u);

    AppUser au =
        new AppUser(u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getStudentId());
    auditService.insert(au, req, "更新个人资料", "更新联系方式或头像");
    return ApiResponse.ok(UserPayload.from(u));
  }

  @PostMapping("/api/auth/password")
  public ApiResponse<Void> changePassword(
      @Valid @RequestBody PasswordChangeRequest body,
      @AuthenticationPrincipal AppUser principal,
      HttpServletRequest req) {
    String np = body.newPassword();
    UserEntity u =
        userRepository
            .findById(principal.id())
            .orElseThrow(() -> ApiException.unauthorized("未登录或登录已失效"));
    if (!passwordEncoder.matches(body.oldPassword(), u.getPasswordHash())) {
      throw ApiException.badRequest("原密码不正确");
    }
    u.setPasswordHash(passwordEncoder.encode(np));
    userRepository.save(u);

    AppUser au =
        new AppUser(u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getStudentId());
    auditService.insert(au, req, "修改登录密码", "用户修改密码");
    return ApiResponse.ok(null);
  }

  private static String normOpt(String s) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
