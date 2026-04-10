package com.studentms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentms.common.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    if (path != null) {
      String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
      if (p.endsWith("/api/auth/login")) {
        filterChain.doFilter(request, response);
        return;
      }
      if (p.startsWith("/uploads/") || "/uploads".equals(p)) {
        filterChain.doFilter(request, response);
        return;
      }
    }

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(7).trim();
    try {
      AppUser user = jwtService.parseUser(token);
      var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (ExpiredJwtException e) {
      write401(response, "登录已过期，请重新登录");
      return;
    } catch (JwtException | IllegalArgumentException e) {
      write401(response, "未登录或登录已失效");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void write401(HttpServletResponse response, String message) throws IOException {
    SecurityContextHolder.clearContext();
    response.setStatus(401);
    response.setContentType("application/json;charset=UTF-8");
    objectMapper.writeValue(response.getOutputStream(), ApiResponse.fail(401, message));
  }
}
