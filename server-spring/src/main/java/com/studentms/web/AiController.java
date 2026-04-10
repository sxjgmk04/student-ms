package com.studentms.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentms.common.ApiException;
import com.studentms.common.ApiResponse;
import com.studentms.config.AiProperties;
import com.studentms.dto.AiStatusResponse;
import com.studentms.dto.AiModelConfigResponse;
import com.studentms.dto.AiModelSwitchRequest;
import com.studentms.dto.ChangeAssistRequest;
import com.studentms.dto.ChangeAssistResponse;
import com.studentms.dto.PolicyQaRequest;
import com.studentms.dto.PolicyQaResponse;
import com.studentms.security.AppUser;
import com.studentms.service.AiEndpointResolver;
import com.studentms.service.AiModelSwitchService;
import com.studentms.service.AuditService;
import com.studentms.service.LlmChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

  private static final Set<String> CHANGE_TYPES = Set.of("转专业", "休学", "复学", "转学");

  private final LlmChatService llmChatService;
  private final AuditService auditService;
  private final ObjectMapper objectMapper;
  private final AiProperties aiProperties;
  private final AiModelSwitchService aiModelSwitchService;
  private final String policyCorpus;

  public AiController(
      LlmChatService llmChatService,
      AuditService auditService,
      ObjectMapper objectMapper,
      AiProperties aiProperties,
      AiModelSwitchService aiModelSwitchService,
      ResourceLoader resourceLoader)
      throws IOException {
    this.llmChatService = llmChatService;
    this.auditService = auditService;
    this.objectMapper = objectMapper;
    this.aiProperties = aiProperties;
    this.aiModelSwitchService = aiModelSwitchService;
    Resource res = resourceLoader.getResource("classpath:ai/policy-corpus.txt");
    if (!res.exists()) {
      this.policyCorpus = "";
    } else {
      this.policyCorpus = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @GetMapping("/api/ai/status")
  @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
  public ApiResponse<AiStatusResponse> aiStatus() {
    boolean enabled = aiProperties.isEnabled();
    boolean keyOk =
        aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank();
    boolean ready = enabled && keyOk;
    String presetRaw = aiModelSwitchService.effectivePreset();
    String presetOut = presetRaw.isEmpty() ? "custom" : presetRaw;
    String effBase = aiModelSwitchService.effectiveBaseUrl();
    String host = hostFromBaseUrl(effBase);
    String label = AiEndpointResolver.providerLabel(presetRaw);
    String model = aiModelSwitchService.effectiveModel();
    return ApiResponse.ok(
        new AiStatusResponse(ready, enabled, presetOut, label, host, model));
  }

  @GetMapping("/api/ai/model-config")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<AiModelConfigResponse> modelConfig() {
    return ApiResponse.ok(aiModelSwitchService.getConfig());
  }

  @PostMapping("/api/ai/model-config")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<AiModelConfigResponse> switchModel(
      @Valid @RequestBody AiModelSwitchRequest body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    AiModelConfigResponse cfg = aiModelSwitchService.switchOption(body.optionKey());
    auditService.insert(user, req, "AI模型切换", "切换为：" + cfg.currentLabel());
    return ApiResponse.ok(cfg);
  }

  private static String hostFromBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return "";
    }
    try {
      URI u = URI.create(baseUrl);
      String h = u.getHost();
      return h != null ? h : "";
    } catch (IllegalArgumentException e) {
      return "";
    }
  }

  @PostMapping("/api/ai/policy-qa")
  @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
  public ApiResponse<PolicyQaResponse> policyQa(
      @Valid @RequestBody PolicyQaRequest body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    String q = body.question().trim();
    if (policyCorpus.isBlank()) {
      throw ApiException.badRequest("政策语料文件缺失，请联系管理员");
    }
    String system = buildPolicySystemPrompt(user);
    String answer = llmChatService.complete(system, q);
    auditService.insert(
        user, req, "AI政策问答", "问题摘要：" + (q.length() > 120 ? q.substring(0, 120) + "…" : q));
    return ApiResponse.ok(new PolicyQaResponse(answer));
  }

  private String buildPolicySystemPrompt(AppUser user) {
    boolean admin = user != null && user.isAdmin();
    String roleInstruction =
        admin
            ? "你面向高校管理人员答复：优先给出审核口径、适用条件、常见边界情形和执行建议；语气专业、简明。"
            : "你面向学生答复：优先给出办理步骤、材料清单、时间节点和注意事项；语气友好、易懂。";
    return "你是高校教务政策咨询助手。只能根据下面「政策摘录」回答；若摘录中没有相关信息，请明确说明"
        + "「提供的材料中未找到相关规定」，并建议用户咨询本校教务处，不要编造条文。\n"
        + roleInstruction
        + "\n\n--- 政策摘录（演示用） ---\n"
        + policyCorpus
        + "\n--- 摘录结束 ---\n\n"
        + "回答使用简洁中文，可分条列出；勿输出与政策无关的闲聊。";
  }

  @PostMapping("/api/ai/change-assist")
  @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
  public ApiResponse<ChangeAssistResponse> changeAssist(
      @Valid @RequestBody ChangeAssistRequest body,
      @AuthenticationPrincipal AppUser user,
      HttpServletRequest req) {
    String desc = body.description().trim();
    String system =
        "你是高校学籍异动申请辅助工具。用户会用口语描述想办的异动。\n"
            + "你必须只输出一个 JSON 对象，不要 markdown 代码块，不要其它解释文字。\n"
            + "JSON 两个字段：\n"
            + "1) type：只能是以下之一：转专业、休学、复学、转学\n"
            + "2) reason：正式书面申请原因，50～220 个汉字以内，第三人称或「本人」均可，用于纸质/系统表单\n"
            + "若信息不足，在 reason 中合理推断并注明「具体情况以学院审核为准」。\n"
            + "用户描述：\n";
    String raw = llmChatService.complete(system, desc);
    String json = extractJsonObject(raw);
    JsonNode node;
    try {
      node = objectMapper.readTree(json);
    } catch (Exception e) {
      throw ApiException.badRequest("模型返回无法解析为 JSON，请手动填写表单");
    }
    String type = node.path("type").asText("").trim();
    String reason = node.path("reason").asText("").trim();
    if (type.isEmpty() || reason.isEmpty()) {
      throw ApiException.badRequest("模型未返回完整的 type 或 reason，请手动填写");
    }
    if (!CHANGE_TYPES.contains(type)) {
      throw ApiException.badRequest(
          "模型返回的异动类型「" + type + "」无效，请从：转专业、休学、复学、转学 中手动选择");
    }
    if (reason.length() > 500) {
      reason = reason.substring(0, 500);
    }
    String note = "以上为 AI 辅助生成，提交前请务必核对类型与原因是否与实际情况一致。";
    auditService.insert(
        user,
        req,
        "AI异动填表辅助",
        "类型建议：" + type + "；描述摘要：" + (desc.length() > 80 ? desc.substring(0, 80) + "…" : desc));
    return ApiResponse.ok(new ChangeAssistResponse(type, reason, note));
  }

  private static String extractJsonObject(String raw) {
    String t = raw.trim();
    int i = t.indexOf('{');
    int j = t.lastIndexOf('}');
    if (i >= 0 && j > i) {
      return t.substring(i, j + 1);
    }
    return t;
  }
}
