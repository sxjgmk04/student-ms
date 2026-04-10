package com.studentms.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studentms.common.ApiExceptionHandler;
import com.studentms.dto.AnalyticsInsightsPayload;
import com.studentms.dto.MajorStatItem;
import com.studentms.entity.RiskFollowUpEntity;
import com.studentms.service.AnalyticsService;
import com.studentms.service.AuditService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

  @Mock private AnalyticsService analyticsService;
  @Mock private AuditService auditService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    AnalyticsController controller = new AnalyticsController(analyticsService, auditService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
  }

  @Test
  void insights_shouldReturnStandardPayload() throws Exception {
    AnalyticsInsightsPayload payload =
        new AnalyticsInsightsPayload(
            "ok",
            List.of("h1"),
            List.of(),
            List.of(new MajorStatItem("计算机", 10, 83.5)),
            12,
            3,
            4,
            68.0,
            75.0,
            60,
            2,
            1,
            "模型说明");
    when(analyticsService.buildInsights()).thenReturn(payload);

    mockMvc
        .perform(get("/api/analytics/insights"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.summary").value("ok"))
        .andExpect(jsonPath("$.data.totalStudentsWithScores").value(12))
        .andExpect(jsonPath("$.data.highAvgThreshold").value(68.0))
        .andExpect(jsonPath("$.data.majorStats[0].major").value("计算机"));
  }

  @Test
  void patchRiskFollowUp_shouldUpdateAndWriteAudit() throws Exception {
    RiskFollowUpEntity followUp = new RiskFollowUpEntity();
    followUp.setStudentId("2024001");
    followUp.setStatus("已沟通");
    followUp.setNote("已电话沟通");
    followUp.setUpdatedAt("2026/04/07 18:00:00");
    when(analyticsService.upsertRiskFollowUp(eq("2024001"), eq("已沟通"), eq("已电话沟通")))
        .thenReturn(followUp);

    mockMvc
        .perform(
            patch("/api/analytics/risk-follow-up/2024001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"已沟通\",\"note\":\"已电话沟通\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.studentId").value("2024001"))
        .andExpect(jsonPath("$.data.status").value("已沟通"))
        .andExpect(jsonPath("$.data.note").value("已电话沟通"));

    verify(analyticsService).upsertRiskFollowUp("2024001", "已沟通", "已电话沟通");
    verify(auditService).insert(any(), any(), eq("学业风险跟进状态更新"), any());
  }
}
