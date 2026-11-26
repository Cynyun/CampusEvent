package com.Cynyun.campusevent.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CR2-03：模拟邮件失败时，报名仍然成功，失败原因写入 email_log。
 */
@SpringBootTest(properties = "app.mock-email.fail=true")
@AutoConfigureMockMvc
class CampusEventEmailFailureTest {

    private static final Path DB_PATH = Path.of("target",
            "email-failure-" + UUID.randomUUID() + ".db").toAbsolutePath();

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("CAMPUSEVENT_DB_PATH", DB_PATH::toString);
        registry.add("app.seed-demo-data", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void emailFailureDoesNotRollbackRegistration() throws Exception {
        MockHttpSession admin = login("admin");
        MockHttpSession student = login("student");
        long activityId = createActivity(admin, "CR2 邮件失败活动", 3);

        MvcResult result = mockMvc.perform(post("/api/student/activities/{id}/registrations", activityId)
                        .session(student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        long registrationId = root.path("data").path("id").asLong();

        assertEquals("APPROVED", jdbcTemplate.queryForObject(
                "SELECT status FROM registration WHERE id = ?", String.class, registrationId));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM email_log WHERE registration_id = ?", Integer.class, registrationId));
        assertEquals("FAILED", jdbcTemplate.queryForObject(
                "SELECT status FROM email_log WHERE registration_id = ?", String.class, registrationId));
        assertEquals("模拟邮件发送失败", jdbcTemplate.queryForObject(
                "SELECT error_message FROM email_log WHERE registration_id = ?", String.class, registrationId));
    }

    private MockHttpSession login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    private long createActivity(MockHttpSession admin, String title, int capacity) throws Exception {
        String startAt = LocalDateTime.now().plusDays(10).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "title", title,
                "category", "验收测试",
                "startAt", startAt,
                "location", "测试教室",
                "capacity", capacity,
                "policyType", "DIRECT"
        ));
        MvcResult result = mockMvc.perform(post("/api/admin/activities")
                        .session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }
}
