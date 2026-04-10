package com.studentms.service;

import com.studentms.common.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarStorageService {

  private static final long MAX_BYTES = 2 * 1024 * 1024L;

  private static final Map<String, String> EXT_BY_TYPE =
      Map.of(
          "image/jpeg", "jpg",
          "image/png", "png",
          "image/gif", "gif",
          "image/webp", "webp");

  @Value("${app.upload-dir:uploads}")
  private String uploadDirProp;

  private Path rootDir() {
    Path p = Paths.get(uploadDirProp);
    if (!p.isAbsolute()) {
      p = Paths.get(System.getProperty("user.dir")).resolve(uploadDirProp);
    }
    return p.normalize().toAbsolutePath();
  }

  private Path avatarsDir() throws IOException {
    Path dir = rootDir().resolve("avatars");
    Files.createDirectories(dir);
    return dir;
  }

  public void deleteManagedFile(String avatarUrl) {
    if (avatarUrl == null || !avatarUrl.startsWith("/uploads/avatars/")) {
      return;
    }
    String name = avatarUrl.substring("/uploads/avatars/".length());
    if (name.isEmpty() || name.contains("..") || name.contains("/") || name.contains("\\")) {
      return;
    }
    try {
      Path target = avatarsDir().resolve(name);
      if (target.normalize().startsWith(avatarsDir().normalize())) {
        Files.deleteIfExists(target);
      }
    } catch (IOException ignored) {
    }
  }

  /** 保存头像，返回形如 /uploads/avatars/{userId}.ext 的路径 */
  public String saveAvatar(long userId, MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw ApiException.badRequest("请选择图片文件");
    }
    if (file.getSize() > MAX_BYTES) {
      throw ApiException.badRequest("图片不能超过 2MB");
    }
    String ct = file.getContentType();
    if (ct == null) {
      throw ApiException.badRequest("无法识别文件类型");
    }
    String lowerCt = ct.toLowerCase(Locale.ROOT).split(";")[0].trim();
    String ext = EXT_BY_TYPE.get(lowerCt);
    if (ext == null) {
      throw ApiException.badRequest("仅支持 JPG、PNG、GIF、WebP 图片");
    }

    Path dir = avatarsDir();
    String prefix = userId + ".";
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, userId + ".*")) {
      for (Path old : ds) {
        Files.deleteIfExists(old);
      }
    }
    String filename = prefix + ext;
    Path target = dir.resolve(filename);
    if (!target.normalize().startsWith(dir.normalize())) {
      throw ApiException.badRequest("无效路径");
    }
    try (InputStream in = file.getInputStream()) {
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
    return "/uploads/avatars/" + filename;
  }
}
