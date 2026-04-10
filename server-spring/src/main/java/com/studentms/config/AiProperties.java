package com.studentms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

  /** 为 false 时不调用外网模型，接口返回明确提示 */
  private boolean enabled = false;

  /** OpenAI 兼容接口根地址，如 https://api.openai.com/v1 或 https://api.deepseek.com/v1 */
  private String baseUrl = "https://api.openai.com/v1";

  private String apiKey = "";

  private String model = "gpt-4o-mini";

  /**
   * 国内厂商快捷预设（仍为默认 base-url / model 时自动替换为对应兼容端点）。可选：siliconflow、deepseek、dashscope；留空表示自定义。
   */
  private String preset = "";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getPreset() {
    return preset;
  }

  public void setPreset(String preset) {
    this.preset = preset;
  }
}
