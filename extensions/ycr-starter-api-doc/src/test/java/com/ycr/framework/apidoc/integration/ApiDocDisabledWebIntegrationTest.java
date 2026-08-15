package com.ycr.framework.apidoc.integration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiDocDisabledWebIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ycr.api-doc.enabled=false",
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true",
        "knife4j.enable=true"
})
class ApiDocDisabledWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("YCR总开关关闭时动态文档和UI资源均应返回404")
    void disabledMasterSwitchShouldCloseAllDocumentationEndpoints() throws Exception {
        assertThat(applicationContext.getBeansOfType(OpenAPI.class)).isEmpty();
        assertThat(applicationContext.getEnvironment()
                .getProperty("springdoc.api-docs.enabled", Boolean.class)).isFalse();
        assertThat(applicationContext.getEnvironment()
                .getProperty("springdoc.swagger-ui.enabled", Boolean.class)).isFalse();
        assertThat(applicationContext.getEnvironment()
                .getProperty("knife4j.enable", Boolean.class)).isFalse();

        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
        mockMvc.perform(get("/doc.html")).andExpect(status().isNotFound());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
