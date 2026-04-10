package com.studentms.service;

import com.studentms.common.ApiException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

  private static final int MAX_ATTEMPTS = 5;
  private static final long LOCK_DURATION_MS = 10 * 60 * 1000L;

  private record AttemptState(int failures, long lockedUntilMs) {}

  private final ConcurrentHashMap<String, AttemptState> byUsername = new ConcurrentHashMap<>();

  public void checkAllowed(String username) {
    if (username == null || username.isBlank()) {
      return;
    }
    String key = username.trim().toLowerCase(Locale.ROOT);
    AttemptState s = byUsername.get(key);
    if (s == null) {
      return;
    }
    long now = System.currentTimeMillis();
    if (s.lockedUntilMs > now) {
      long remainMin = Math.max(1, (s.lockedUntilMs - now + 59_999) / 60_000);
      throw ApiException.tooManyRequests("登录尝试过多，请 " + remainMin + " 分钟后再试");
    }
  }

  public void onFailure(String username) {
    if (username == null || username.isBlank()) {
      return;
    }
    String key = username.trim().toLowerCase(Locale.ROOT);
    long now = System.currentTimeMillis();
    byUsername.compute(
        key,
        (k, old) -> {
          if (old != null && old.lockedUntilMs > now) {
            return old;
          }
          int prevFails = (old == null || old.lockedUntilMs > now) ? 0 : old.failures;
          int fails = prevFails + 1;
          if (fails >= MAX_ATTEMPTS) {
            return new AttemptState(0, now + LOCK_DURATION_MS);
          }
          return new AttemptState(fails, 0L);
        });
  }

  public void onSuccess(String username) {
    if (username == null || username.isBlank()) {
      return;
    }
    byUsername.remove(username.trim().toLowerCase(Locale.ROOT));
  }
}
