package com.studentms.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studentms.common.ApiExceptionHandler;
import com.studentms.entity.StudentEntity;
import com.studentms.repo.StudentRepository;
import com.studentms.security.AppUser;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class StudentSelfControllerTest {

  @Mock private StudentRepository studentRepository;

  private MockMvc mockMvc;

  private static final AppUser STU =
      new AppUser(1L, "student1", "张三", "student", "2021001");

  @BeforeEach
  void setUp() {
    StudentSelfController controller = new StudentSelfController(studentRepository);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ApiExceptionHandler())
            .setCustomArgumentResolvers(
                new HandlerMethodArgumentResolver() {
                  @Override
                  public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(AppUser.class);
                  }

                  @Override
                  public Object resolveArgument(
                      MethodParameter parameter,
                      ModelAndViewContainer mavContainer,
                      NativeWebRequest webRequest,
                      WebDataBinderFactory binderFactory) {
                    return STU;
                  }
                })
            .build();
  }

  @Test
  void me_shouldReturn404WhenNoStudentRecord() throws Exception {
    when(studentRepository.findByStudentId(eq("2021001"))).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/students/me"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404));
  }

  @Test
  void me_shouldReturnPayloadWhenFound() throws Exception {
    StudentEntity e = new StudentEntity();
    e.setId(1L);
    e.setStudentId("2021001");
    e.setName("张三");
    e.setGender("男");
    e.setMajor("计算机");
    e.setClassName("计科2021-1");
    e.setGrade(2021);
    e.setStatus("在读");
    when(studentRepository.findByStudentId(eq("2021001"))).thenReturn(Optional.of(e));

    mockMvc
        .perform(get("/api/students/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.studentId").value("2021001"))
        .andExpect(jsonPath("$.data.name").value("张三"))
        .andExpect(jsonPath("$.data.major").value("计算机"));
  }
}
