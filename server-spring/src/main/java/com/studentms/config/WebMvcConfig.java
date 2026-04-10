package com.studentms.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path root = Path.of(uploadDir);
    if (!root.isAbsolute()) {
      root = Path.of(System.getProperty("user.dir")).resolve(uploadDir);
    }
    root = root.normalize().toAbsolutePath();
    String loc = root.toUri().toASCIIString();
    if (!loc.endsWith("/")) {
      loc += "/";
    }
    registry.addResourceHandler("/uploads/**").addResourceLocations(loc);
  }
}
