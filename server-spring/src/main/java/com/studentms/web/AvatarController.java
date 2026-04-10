package com.studentms.web;

import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.dto.UserPayload;
import com.studentms.entity.UserEntity;
import com.studentms.repo.UserRepository;
import com.studentms.security.AppUser;
import com.studentms.service.AuditService;
import com.studentms.service.AvatarStorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AvatarController {

  private final UserRepository userRepository;
  private final AvatarStorageService avatarStorageService;
  private final AuditService auditService;

  public AvatarController(
      UserRepository userRepository,
      AvatarStorageService avatarStorageService,
      AuditService auditService) {
    this.userRepository = userRepository;
    this.avatarStorageService = avatarStorageService;
    this.auditService = auditService;
  }

  @PostMapping(value = "/api/auth/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<UserPayload> uploadAvatar(
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal AppUser principal,
      HttpServletRequest req) {
    UserEntity u =
        userRepository
            .findById(principal.id())
            .orElseThrow(() -> ApiException.unauthorized("未登录或登录已失效"));

    String oldUrl = u.getAvatarUrl();
    final String path;
    try {
      path = avatarStorageService.saveAvatar(u.getId(), file);
    } catch (IOException e) {
      throw ApiException.badRequest("保存头像失败");
    }
    u.setAvatarUrl(path);
    userRepository.save(u);

    if (oldUrl != null && !oldUrl.equals(path)) {
      avatarStorageService.deleteManagedFile(oldUrl);
    }

    AppUser au =
        new AppUser(u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getStudentId());
    auditService.insert(au, req, "上传头像", "更新用户头像文件");
    return ApiResponse.ok(UserPayload.from(u));
  }
}
