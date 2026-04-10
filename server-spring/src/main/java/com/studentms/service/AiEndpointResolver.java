package com.studentms.service;

import com.studentms.config.AiProperties;
import java.util.Locale;

/**
 * 根据 {@code app.ai.preset} 在「仍为默认 OpenAI 地址/模型」时自动切换为国内常用 OpenAI 兼容端点（需自行申请
 * Key；免费额度以各平台最新规则为准）。
 */
public final class AiEndpointResolver {

  public static final String DEFAULT_OPENAI_BASE = "https://api.openai.com/v1";
  public static final String DEFAULT_OPENAI_MODEL = "gpt-4o-mini";

  private AiEndpointResolver() {}

  public static String effectiveBaseUrl(AiProperties p) {
    String raw = p.getBaseUrl() == null ? "" : p.getBaseUrl().trim();
    String preset = normalizePreset(p.getPreset());
    if (!raw.isEmpty() && !raw.equalsIgnoreCase(DEFAULT_OPENAI_BASE)) {
      return stripTrailingSlashes(raw);
    }
    String fromPreset = baseForPreset(preset);
    if (fromPreset != null) {
      return fromPreset;
    }
    return raw.isEmpty() ? DEFAULT_OPENAI_BASE : stripTrailingSlashes(raw);
  }

  public static String effectiveModel(AiProperties p) {
    String raw = p.getModel() == null ? "" : p.getModel().trim();
    String preset = normalizePreset(p.getPreset());
    if (!raw.isEmpty() && !raw.equalsIgnoreCase(DEFAULT_OPENAI_MODEL)) {
      return raw;
    }
    String fromPreset = modelForPreset(preset);
    if (fromPreset != null) {
      return fromPreset;
    }
    return raw.isEmpty() ? DEFAULT_OPENAI_MODEL : raw;
  }

  /** 方舟豆包预设下 model 必须为用户在控制台创建的接入点 ID（ep-…），不能用默认占位模型名。 */
  public static boolean isDoubaoPreset(AiProperties p) {
    String preset = normalizePreset(p.getPreset());
    return "doubao".equals(preset) || "ark".equals(preset);
  }

  public static String providerLabel(String preset) {
    return switch (normalizePreset(preset)) {
      case "siliconflow" -> "硅基流动（OpenAI 兼容）";
      case "deepseek" -> "DeepSeek 官方 API";
      case "dashscope" -> "阿里云百炼（兼容模式）";
      case "doubao", "ark" -> "火山引擎方舟（豆包，OpenAI 兼容）";
      default -> "自定义（OpenAI 兼容）";
    };
  }

  private static String normalizePreset(String preset) {
    if (preset == null || preset.isBlank()) {
      return "";
    }
    return preset.trim().toLowerCase(Locale.ROOT);
  }

  private static String baseForPreset(String preset) {
    return switch (preset) {
      case "siliconflow" -> "https://api.siliconflow.cn/v1";
      case "deepseek" -> "https://api.deepseek.com/v1";
      case "dashscope" -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
      case "doubao", "ark" -> "https://ark.cn-beijing.volces.com/api/v3";
      default -> null;
    };
  }

  /**
   * 返回 {@code null} 表示沿用 YAML/环境变量中的 model；返回空串表示须用户显式配置 model（如豆包接入点
   * ID）。
   */
  private static String modelForPreset(String preset) {
    return switch (preset) {
      case "siliconflow" -> "Qwen/Qwen2.5-14B-Instruct";
      case "deepseek" -> "deepseek-chat";
      case "dashscope" -> "qwen-turbo";
      case "doubao", "ark" -> "";
      default -> null;
    };
  }

  private static String stripTrailingSlashes(String s) {
    return s.replaceAll("/+$", "");
  }
}
