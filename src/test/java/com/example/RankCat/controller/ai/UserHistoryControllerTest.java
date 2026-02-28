package com.example.RankCat.controller.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.RankCat.config.oauth.WebOAuthSecurityConfig;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import com.example.RankCat.service.user.impl.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = UserHistoryController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = WebOAuthSecurityConfig.class)
        })
class UserHistoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserHistoryService userHistoryService;
    @MockBean private UserService userService;

    @Test
    @DisplayName("로그인 유저 객체 생성 실패 시, 핸들러가 C002(500 에러)를 뱉는지 확인한다")
    void getHistories_bindingFailure_throwsC002() throws Exception {
        // 억지로 에러가 날 만한 상황(예: 유저 정보 없이 요청)을 만들었을 때
        mockMvc.perform(get("/ai/histories"))
                .andExpect(status().isInternalServerError()) // 500 에러를 기대함
                .andExpect(jsonPath("$.code").value("C002")); // 우리가 정한 에러 코드가 나오는지 확인
    }
}
