package com.studentms.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.studentms.config.AiProperties;
import org.junit.jupiter.api.Test;

class AiEndpointResolverTest {

  @Test
  void presetSiliconflowOverridesDefaults() {
    AiProperties p = new AiProperties();
    p.setPreset("siliconflow");
    assertThat(AiEndpointResolver.effectiveBaseUrl(p)).isEqualTo("https://api.siliconflow.cn/v1");
    assertThat(AiEndpointResolver.effectiveModel(p)).isEqualTo("Qwen/Qwen2.5-14B-Instruct");
  }

  @Test
  void explicitBaseUrlWinsOverPreset() {
    AiProperties p = new AiProperties();
    p.setPreset("siliconflow");
    p.setBaseUrl("https://example.com/v1");
    assertThat(AiEndpointResolver.effectiveBaseUrl(p)).isEqualTo("https://example.com/v1");
  }

  @Test
  void emptyPresetKeepsOpenAiDefaults() {
    AiProperties p = new AiProperties();
    assertThat(AiEndpointResolver.effectiveBaseUrl(p)).isEqualTo("https://api.openai.com/v1");
    assertThat(AiEndpointResolver.effectiveModel(p)).isEqualTo("gpt-4o-mini");
  }

  @Test
  void presetDeepseek() {
    AiProperties p = new AiProperties();
    p.setPreset("deepseek");
    assertThat(AiEndpointResolver.effectiveBaseUrl(p)).isEqualTo("https://api.deepseek.com/v1");
    assertThat(AiEndpointResolver.effectiveModel(p)).isEqualTo("deepseek-chat");
  }

  @Test
  void presetDoubaoUsesArkBaseAndRequiresEpModel() {
    AiProperties p = new AiProperties();
    p.setPreset("doubao");
    assertThat(AiEndpointResolver.effectiveBaseUrl(p))
        .isEqualTo("https://ark.cn-beijing.volces.com/api/v3");
    assertThat(AiEndpointResolver.effectiveModel(p)).isEmpty();
    assertThat(AiEndpointResolver.isDoubaoPreset(p)).isTrue();
  }

  @Test
  void presetDoubaoWithExplicitModel() {
    AiProperties p = new AiProperties();
    p.setPreset("ark");
    p.setModel("ep-20250101-demo");
    assertThat(AiEndpointResolver.effectiveModel(p)).isEqualTo("ep-20250101-demo");
  }
}
