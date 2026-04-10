package com.studentms.service;

import com.studentms.common.ApiException;
import com.studentms.config.AiProperties;
import com.studentms.dto.AiModelConfigResponse;
import com.studentms.dto.AiModelOption;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AiModelSwitchService {

  private static final String OPTION_DEFAULT = "default";

  private static final List<AiModelOption> OPTIONS =
      List.of(
          new AiModelOption(OPTION_DEFAULT, "默认配置（跟随环境变量）", "", ""),
          new AiModelOption("siliconflow-qwen14b", "硅基流动 · Qwen2.5-14B", "siliconflow", "Qwen/Qwen2.5-14B-Instruct"),
          new AiModelOption("deepseek-chat", "DeepSeek · deepseek-chat", "deepseek", "deepseek-chat"),
          new AiModelOption("dashscope-qwen", "阿里百炼 · qwen-turbo", "dashscope", "qwen-turbo"));

  private final AiProperties props;
  private volatile String selectedKey = OPTION_DEFAULT;

  public AiModelSwitchService(AiProperties props) {
    this.props = props;
  }

  public AiModelConfigResponse getConfig() {
    AiModelOption selected = getSelectedOption();
    String preset = selected.preset().isBlank() ? normalizePreset(props.getPreset()) : selected.preset();
    String model = selected.model().isBlank() ? AiEndpointResolver.effectiveModel(props) : selected.model();
    String label =
        OPTION_DEFAULT.equals(selected.key())
            ? "默认配置（跟随环境变量）"
            : selected.label();
    return new AiModelConfigResponse(selected.key(), label, preset, model, OPTIONS);
  }

  public AiModelConfigResponse switchOption(String optionKey) {
    String k = optionKey == null ? "" : optionKey.trim().toLowerCase(Locale.ROOT);
    boolean exists = OPTIONS.stream().anyMatch(o -> o.key().equals(k));
    if (!exists) {
      throw ApiException.badRequest("无效的模型选项，请刷新后重试");
    }
    selectedKey = k;
    return getConfig();
  }

  public String effectiveBaseUrl() {
    AiModelOption selected = getSelectedOption();
    if (!selected.preset().isBlank()) {
      return baseForPreset(selected.preset());
    }
    return AiEndpointResolver.effectiveBaseUrl(props);
  }

  public String effectiveModel() {
    AiModelOption selected = getSelectedOption();
    if (!selected.model().isBlank()) {
      return selected.model();
    }
    return AiEndpointResolver.effectiveModel(props);
  }

  public String effectivePreset() {
    AiModelOption selected = getSelectedOption();
    if (!selected.preset().isBlank()) {
      return selected.preset();
    }
    return normalizePreset(props.getPreset());
  }

  private AiModelOption getSelectedOption() {
    return OPTIONS.stream()
        .filter(o -> o.key().equals(selectedKey))
        .findFirst()
        .orElse(OPTIONS.get(0));
  }

  private static String normalizePreset(String preset) {
    return preset == null ? "" : preset.trim();
  }

  private static String baseForPreset(String preset) {
    return switch (preset) {
      case "siliconflow" -> "https://api.siliconflow.cn/v1";
      case "deepseek" -> "https://api.deepseek.com/v1";
      case "dashscope" -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
      case "doubao", "ark" -> "https://ark.cn-beijing.volces.com/api/v3";
      default -> AiEndpointResolver.DEFAULT_OPENAI_BASE;
    };
  }
}

