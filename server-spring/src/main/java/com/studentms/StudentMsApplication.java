package com.studentms;

import com.studentms.config.AiProperties;
import com.studentms.config.AnalyticsRuleProperties;
import com.studentms.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AnalyticsRuleProperties.class, AiProperties.class})
public class StudentMsApplication {

  public static void main(String[] args) {
    SpringApplication.run(StudentMsApplication.class, args);
  }
}
