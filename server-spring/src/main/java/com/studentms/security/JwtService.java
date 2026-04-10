package com.studentms.security;

import com.studentms.config.JwtProperties;
import com.studentms.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties props;

  public JwtService(JwtProperties props) {
    this.props = props;
  }

  private SecretKey key() {
    byte[] raw = props.secret().getBytes(StandardCharsets.UTF_8);
    if (raw.length < 32) {
      try {
        raw = MessageDigest.getInstance("SHA-256").digest(raw);
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException(e);
      }
    }
    return Keys.hmacShaKeyFor(raw);
  }

  public String createToken(UserEntity user) {
    long now = System.currentTimeMillis();
    Date exp = new Date(now + props.expirationMs());
    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .claim("username", user.getUsername())
        .claim("name", user.getName())
        .claim("role", user.getRole())
        .claim("studentId", user.getStudentId())
        .issuedAt(new Date(now))
        .expiration(exp)
        .signWith(key())
        .compact();
  }

  public AppUser parseUser(String token) {
    Claims c = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    Long id = Long.parseLong(c.getSubject());
    String username = c.get("username", String.class);
    String name = c.get("name", String.class);
    String role = c.get("role", String.class);
    String studentId = c.get("studentId", String.class);
    return new AppUser(id, username, name, role, studentId);
  }
}
