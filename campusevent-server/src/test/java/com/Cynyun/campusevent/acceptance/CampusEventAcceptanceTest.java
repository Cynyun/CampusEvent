package com.Cynyun.campusevent.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 实验二初始版 C1—C10 的可重复集成验收。
 *
 * <p>测试使用独立的 SQLite 文件，并关闭演示活动/学生初始化；
 * DemoDataSeeder 仍会创建 student 与 admin 两个演示账号，测试直接使用。</p>
 *
 * <p>C10 在测试内用独立 JDBC 连接校验数据已落到同一个数据库文件；
 * 真正的“停服—重启—再查询”由同包的 CampusEventRestartTest 用真实 Tomcat 完成。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CampusEventAcceptanceTest {

    private static final Path DB_PATH = Path.of("target",
            "acceptance-" + UUID.randomUUID() + ".db").toAbsolutePath();

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("CAMPUSEVENT_DB_PATH", DB_PATH::toString);
        registry.add("app.seed-demo-data", () -> "false");
    }

    private static final String DEMO_PASSWORD_HASH =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"; // 123456

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession adminSession;
    private MockHttpSession student01Session;
    private MockHttpSession student02Session;
    private MockHttpSession student03Session;
    private MockHttpSession student04Session;

    private long directActivityId;
    private long approvalActivityId;
    private long capacityActivityId;
    private long student01DirectRegistrationId;
    private long student01ApprovalRegistrationId;
    private long student02ApprovalRegistrationId;
    private long student03CapacityRegistrationId;
    private long student04CapacityRegistrationId;

    @BeforeAll
    void createExtraStudentAccounts() {
        for (int i = 2; i <= 4; i++) {
            String id = String.format("student%02d", i);
            jdbcTemplate.update(
                    "INSERT INTO student (student_id, name, grade, password_hash) " +
                            "SELECT ?, ?, ?, ? WHERE NOT EXISTS " +
                            "(SELECT 1 FROM student WHERE student_id = ?)",
                    id, "验收学生" + i, i, DEMO_PASSWORD_HASH, id);
        }
        // student01 使用演示账号 student；CR1 需要大一学生。
        jdbcTemplate.update("UPDATE student SET grade = 1 WHERE student_id = 'student'");
    }

    @BeforeEach
    void loginAllRoles() throws Exception {
        if (adminSession == null) {
            adminSession = login("admin");
            student01Session = login("student");
            student02Session = login("student02");
            student03Session = login("student03");
            student04Session = login("student04");
        }
    }

    @Test
    @Order(1)
    void c1CreateActivitiesAndReadThemBack() throws Exception {
        directActivityId = createActivity("C1 直接报名活动", "DIRECT", 2);
        approvalActivityId = createActivity("C5 审核制活动", "APPROVAL", 5);

        mockMvc.perform(get("/api/activities/{id}", directActivityId).session(student01Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) directActivityId))
                .andExpect(jsonPath("$.data.title").value("C1 直接报名活动"))
                .andExpect(jsonPath("$.data.policyType").value("DIRECT"))
                .andExpect(jsonPath("$.data.capacity").value(2));
    }

    @Test
    @Order(2)
    void c2DirectRegistrationIsApprovedAndCreatesMessage() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/student/activities/{id}/registrations", directActivityId)
                        .session(student01Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andReturn();
        student01DirectRegistrationId = dataId(result);

        mockMvc.perform(get("/api/student/messages").session(student01Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.activityId == " + directActivityId + ")].content")
                        .value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("报名成功"))));
    }

    @Test
    @Order(3)
    void c3DuplicateRegistrationIsRejected() throws Exception {
        mockMvc.perform(post("/api/student/activities/{id}/registrations", directActivityId)
                        .session(student01Session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("你已报名该活动，请勿重复提交"));
    }

    @Test
    @Order(4)
    void c4CapacityLimitBlocksExtraRegistration() throws Exception {
        mockMvc.perform(post("/api/student/activities/{id}/registrations", directActivityId)
                        .session(student02Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/student/activities/{id}/registrations", directActivityId)
                        .session(student03Session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("活动名额已满"));
    }

    @Test
    @Order(5)
    void c5ApprovalRegistrationStartsPendingAndDoesNotOccupySeat() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/student/activities/{id}/registrations", approvalActivityId)
                        .session(student01Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        student01ApprovalRegistrationId = dataId(result);

        mockMvc.perform(get("/api/admin/registrations")
                        .param("status", "PENDING")
                        .param("activityId", String.valueOf(approvalActivityId))
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + student01ApprovalRegistrationId + ")]").exists());

        mockMvc.perform(get("/api/admin/statistics").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.activityId == " + approvalActivityId + ")].approvedCount").value(0));
    }

    @Test
    @Order(6)
    void c6AdminApprovalChangesStatusAndNotifiesStudent() throws Exception {
        mockMvc.perform(post("/api/admin/registrations/{id}/approve", student01ApprovalRegistrationId)
                        .session(adminSession))
                .andExpect(status().isOk());

        assertEquals("APPROVED", studentRegistrationStatus(student01Session, student01ApprovalRegistrationId));
        mockMvc.perform(get("/api/student/messages").session(student01Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.activityId == " + approvalActivityId + ")].content")
                        .value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("报名已通过"))));
    }

    @Test
    @Order(7)
    void c7AdminRejectionChangesStatusAndNotifiesStudent() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/student/activities/{id}/registrations", approvalActivityId)
                        .session(student02Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        student02ApprovalRegistrationId = dataId(result);

        mockMvc.perform(post("/api/admin/registrations/{id}/reject", student02ApprovalRegistrationId)
                        .session(adminSession))
                .andExpect(status().isOk());

        assertEquals("REJECTED", studentRegistrationStatus(student02Session, student02ApprovalRegistrationId));
        mockMvc.perform(get("/api/student/messages").session(student02Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.activityId == " + approvalActivityId + ")].content")
                        .value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("报名未通过"))));
    }

    @Test
    @Order(8)
    void c8ApprovalRechecksCapacityAndKeepsOverflowPending() throws Exception {
        capacityActivityId = createActivity("C8 名额竞争活动", "APPROVAL", 1);

        MvcResult third = mockMvc.perform(post("/api/student/activities/{id}/registrations", capacityActivityId)
                        .session(student03Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        student03CapacityRegistrationId = dataId(third);
        mockMvc.perform(post("/api/admin/registrations/{id}/approve", student03CapacityRegistrationId)
                        .session(adminSession))
                .andExpect(status().isOk());

        MvcResult fourth = mockMvc.perform(post("/api/student/activities/{id}/registrations", capacityActivityId)
                        .session(student04Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        student04CapacityRegistrationId = dataId(fourth);
        mockMvc.perform(post("/api/admin/registrations/{id}/approve", student04CapacityRegistrationId)
                        .session(adminSession))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("名额已满，请先拒绝或等待其他报名取消"));

        assertEquals("PENDING", studentRegistrationStatus(student04Session, student04CapacityRegistrationId));
    }

    @Test
    @Order(9)
    void c9CancelApprovedAndPendingRegistrationsFreesSeats() throws Exception {
        mockMvc.perform(delete("/api/student/registrations/{id}", student03CapacityRegistrationId)
                        .session(student03Session))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/student/registrations/{id}", student04CapacityRegistrationId)
                        .session(student04Session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/statistics").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.activityId == " + capacityActivityId + ")].approvedCount").value(0))
                .andExpect(jsonPath("$.data[?(@.activityId == " + capacityActivityId + ")].pendingCount").value(0));
    }

    @Test
    @Order(10)
    void c10DataIsPersistedInTheSqliteFile() throws Exception {
        assertTrue(DB_PATH.toFile().isFile(), "SQLite 数据库文件应已创建");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             Statement statement = connection.createStatement()) {
            assertEquals(3, count(statement, "SELECT COUNT(*) FROM activity"));
            assertEquals(3, count(statement, "SELECT COUNT(*) FROM registration WHERE status = 'APPROVED'"),
                    "直接报名 2 条 + 审核通过 1 条");
            assertEquals(1, count(statement, "SELECT COUNT(*) FROM registration WHERE status = 'REJECTED'"));
            assertEquals(2, count(statement, "SELECT COUNT(*) FROM registration WHERE status = 'CANCELLED'"));
            int notificationCount = count(statement, "SELECT COUNT(*) FROM message WHERE content LIKE '报名已通过%'")
                    + count(statement, "SELECT COUNT(*) FROM message WHERE content LIKE '报名成功%'")
                    + count(statement, "SELECT COUNT(*) FROM message WHERE content LIKE '报名未通过%'")
                    + count(statement, "SELECT COUNT(*) FROM message WHERE content LIKE '报名已取消%'")
                    + count(statement, "SELECT COUNT(*) FROM message WHERE content LIKE '报名已提交%'");
            assertEquals(11, notificationCount, "通知应全部持久化（直接报名 2 + 审核提交 4 + 审批/拒绝 2 + 取消 2 + 通过 1）");
        }
    }

    @Test
    @Order(11)
    void cr1GradeLimitedDirectRegistrationAllowsOnlySophomoresAndJuniors() throws Exception {
        long activityId = createActivity("CR1 年级限定活动", "GRADE_LIMITED_DIRECT", 5);

        mockMvc.perform(post("/api/student/activities/{id}/registrations", activityId)
                        .session(student01Session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("该活动仅限大二、大三学生报名"));

        mockMvc.perform(post("/api/student/activities/{id}/registrations", activityId)
                        .session(student02Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/student/activities/{id}/registrations", activityId)
                        .session(student03Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/student/activities/{id}/registrations", activityId)
                        .session(student04Session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("该活动仅限大二、大三学生报名"));
    }

    @Test
    @Order(12)
    void cr2EmailLogsAreWrittenAndDuplicateActionsDoNotRepeatThem() throws Exception {
        long directId = createActivity("CR2 直接报名邮件", "DIRECT", 5);
        MvcResult directResult = mockMvc.perform(post("/api/student/activities/{id}/registrations", directId)
                        .session(student01Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andReturn();
        long directRegistrationId = dataId(directResult);
        assertEquals(1, emailCount(directRegistrationId));
        assertEquals("SENT", emailStatus(directRegistrationId));

        mockMvc.perform(post("/api/student/activities/{id}/registrations", directId)
                        .session(student01Session))
                .andExpect(status().isConflict());
        assertEquals(1, emailCount(directRegistrationId), "重复报名不能重复发邮件");

        long approvalId = createActivity("CR2 审核邮件", "APPROVAL", 5);
        MvcResult pendingResult = mockMvc.perform(post("/api/student/activities/{id}/registrations", approvalId)
                        .session(student02Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        long pendingRegistrationId = dataId(pendingResult);
        assertEquals(0, emailCount(pendingRegistrationId), "待审核不发邮件");

        mockMvc.perform(post("/api/admin/registrations/{id}/approve", pendingRegistrationId)
                        .session(adminSession))
                .andExpect(status().isOk());
        assertEquals(1, emailCount(pendingRegistrationId));
        assertEquals("APPROVED", emailEventType(pendingRegistrationId));

        MvcResult rejectResult = mockMvc.perform(post("/api/student/activities/{id}/registrations", approvalId)
                        .session(student03Session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        long rejectRegistrationId = dataId(rejectResult);
        mockMvc.perform(post("/api/admin/registrations/{id}/reject", rejectRegistrationId)
                        .session(adminSession))
                .andExpect(status().isOk());
        assertEquals(1, emailCount(rejectRegistrationId));
        assertEquals("REJECTED", emailEventType(rejectRegistrationId));
    }

    private int emailCount(long registrationId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM email_log WHERE registration_id = ?", Integer.class, registrationId);
        return count == null ? 0 : count;
    }

    private String emailStatus(long registrationId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM email_log WHERE registration_id = ? ORDER BY id DESC LIMIT 1",
                String.class, registrationId);
    }

    private String emailEventType(long registrationId) {
        return jdbcTemplate.queryForObject(
                "SELECT event_type FROM email_log WHERE registration_id = ? ORDER BY id DESC LIMIT 1",
                String.class, registrationId);
    }

    private MockHttpSession login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session, "登录后应创建 HttpSession");
        return session;
    }

    private long createActivity(String title, String policyType, int capacity) throws Exception {
        String startAt = LocalDateTime.now().plusDays(10).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "title", title,
                "category", "验收测试",
                "startAt", startAt,
                "location", "测试教室",
                "capacity", capacity,
                "policyType", policyType
        ));
        MvcResult result = mockMvc.perform(post("/api/admin/activities")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(title))
                .andReturn();
        return dataId(result);
    }

    private long dataId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private String studentRegistrationStatus(MockHttpSession session, long registrationId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student/registrations").session(session))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        for (JsonNode item : data) {
            if (item.path("id").asLong() == registrationId) {
                return item.path("status").asText();
            }
        }
        return "NOT_FOUND";
    }

    private int count(Statement statement, String sql) throws Exception {
        try (ResultSet rs = statement.executeQuery(sql)) {
            assertTrue(rs.next(), "COUNT 查询应有结果");
            return rs.getInt(1);
        }
    }
}