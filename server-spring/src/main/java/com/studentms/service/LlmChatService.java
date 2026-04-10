package com.studentms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentms.common.ApiException;
import com.studentms.config.AiProperties;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class LlmChatService {

  private static final int MAX_USER_CHARS = 3000;

  private final AiProperties props;
  private final ObjectMapper objectMapper;
  private final AiModelSwitchService aiModelSwitchService;

  public LlmChatService(
      AiProperties props, ObjectMapper objectMapper, AiModelSwitchService aiModelSwitchService) {
    this.props = props;
    this.objectMapper = objectMapper;
    this.aiModelSwitchService = aiModelSwitchService;
  }

  public String complete(String systemPrompt, String userMessage) {
    ensureConfigured();
    String u = truncate(userMessage, MAX_USER_CHARS);
    String s = truncate(systemPrompt, 12000);

    String base = aiModelSwitchService.effectiveBaseUrl();
    RestClient client = RestClient.builder().baseUrl(base).build();

    var messages = new ArrayList<Map<String, String>>();
    messages.add(Map.of("role", "system", "content", s));
    messages.add(Map.of("role", "user", "content", u));

    String model = aiModelSwitchService.effectiveModel();
    if (model == null || model.isBlank()) {
      throw ApiException.badRequest(
          AiEndpointResolver.isDoubaoPreset(props)
              ? "豆包（火山方舟）：请在 AI_MODEL / app.ai.model 中填写方舟控制台「推理接入点」ID（一般为 ep- 开头），并配置对应 API Key"
              : "未配置有效的模型名称，请设置 AI_MODEL / app.ai.model");
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", model);
    body.put("messages", messages);
    body.put("temperature", 0.35);

    String json;
    try {
      json = objectMapper.writeValueAsString(body);
    } catch (Exception e) {
      throw ApiException.badRequest("构造模型请求失败");
    }

    String raw;
    try {
      raw =
          client
              .post()
              .uri("/chat/completions")
              .contentType(MediaType.APPLICATION_JSON)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey().trim())
              .body(json)
              .retrieve()
              .body(String.class);
    } catch (RestClientResponseException e) {
      String friendly = humanizeLlmHttpError(e);
      if (friendly != null) {
        throw ApiException.badRequest(friendly);
      }
      String hint = e.getResponseBodyAsString(StandardCharsets.UTF_8);
      if (hint != null && hint.length() > 200) {
        hint = hint.substring(0, 200) + "…";
      }
      throw ApiException.badRequest(
          "大模型接口错误（HTTP " + e.getStatusCode().value() + "）" + (hint != null ? "：" + hint : ""));
    } catch (Exception e) {
      throw ApiException.badRequest("调用大模型失败：" + e.getMessage());
    }

    if (raw == null || raw.isBlank()) {
      throw ApiException.badRequest("大模型返回为空");
    }

    try {
      JsonNode root = objectMapper.readTree(raw);
      if (root.has("error")) {
        String msg = root.path("error").path("message").asText("未知错误");
        throw ApiException.badRequest("大模型错误：" + msg);
      }
      JsonNode choices = root.path("choices");
      if (!choices.isArray() || choices.isEmpty()) {
        throw ApiException.badRequest("大模型响应格式异常：无 choices");
      }
      String content =
          choices.get(0).path("message").path("content").asText("").trim();
      if (content.isEmpty()) {
        throw ApiException.badRequest("大模型未返回有效内容");
      }
      return content;
    } catch (ApiException e) {
      throw e;
    } catch (Exception e) {
      throw ApiException.badRequest("解析大模型响应失败");
    }
  }

  public void ensureConfigured() {
    if (!props.isEnabled()) {
      throw ApiException.badRequest(
          "AI 功能未启用：请设置 AI_ENABLED=true（或 app.ai.enabled）并配置 AI_API_KEY；国内用户可设 AI_PRESET=siliconflow 等，详见服务端配置说明");
    }
    if (props.getApiKey() == null || props.getApiKey().isBlank()) {
      throw ApiException.badRequest("未配置大模型 API Key：请设置 app.ai.api-key 或环境变量 AI_API_KEY");
    }
  }

  /** 将常见 HTTP 错误转成更易懂的中文（仍依赖各平台返回体关键词）。 */
  private static String humanizeLlmHttpError(RestClientResponseException e) {
    int status = e.getStatusCode().value();
    String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
    if (body == null) {
      body = "";
    }
    String lowerBody = body.toLowerCase();
    boolean balanceInsufficient =
        status == 402
            || (status == 403 && body.contains("\"code\":30001"))
            || lowerBody.contains("insufficient balance")
            || lowerBody.contains("account balance is insufficient")
            || lowerBody.contains("balance is insufficient");
    if (balanceInsufficient) {
      return "大模型账号余额或可用额度不足。请到对应平台充值或确认赠送额度是否已用完；若不想付费，可切换到仍有免费额度的服务商。";
    }
    if (status == 401) {
      return "大模型 API Key 无效或已过期（HTTP 401），请检查 AI_API_KEY 是否与当前平台一致、是否复制完整。";
    }
    if (status == 429 || body.toLowerCase().contains("rate limit")) {
      return "请求过于频繁或触发限流（HTTP 429），请稍后再试。";
    }
    return null;
  }

  private static String truncate(String s, int max) {
    if (s == null) return "";
    if (s.length() <= max) return s;
    return s.substring(0, max) + "…（已截断）";
  }
}
