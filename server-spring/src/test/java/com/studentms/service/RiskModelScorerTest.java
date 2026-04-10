package com.studentms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RiskModelScorerTest {

  @Test
  void lowerAvg_shouldIncreaseProbability() {
    double high = RiskModelScorer.probability(55, 0, "平稳");
    double low = RiskModelScorer.probability(85, 0, "平稳");
    assertTrue(high > low);
  }

  @Test
  void moreFails_shouldIncreaseProbability() {
    double many = RiskModelScorer.probability(75, 3, "平稳");
    double none = RiskModelScorer.probability(75, 0, "平稳");
    assertTrue(many > none);
  }

  @Test
  void decliningTrend_shouldIncreaseProbability() {
    double down = RiskModelScorer.probability(72, 0, "下降");
    double up = RiskModelScorer.probability(72, 0, "上升");
    assertTrue(down > up);
  }

  @Test
  void probability_shouldStayInUnitInterval() {
    double p = RiskModelScorer.probability(40, 5, "下降");
    assertTrue(p >= 0 && p <= 1);
  }

  @Test
  void riskLevelLabel_shouldMapTiers() {
    assertEquals("高", RiskModelScorer.riskLevelLabel(0.55));
    assertEquals("中", RiskModelScorer.riskLevelLabel(0.4));
    assertEquals("低", RiskModelScorer.riskLevelLabel(0.2));
  }
}
