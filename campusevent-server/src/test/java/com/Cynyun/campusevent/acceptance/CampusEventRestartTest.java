package com.Cynyun.campusevent.acceptance;

import com.Cynyun.campusevent.CampusEventApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C10 的真实重启验收：同一 SQLite 文件先启动一次服务写入活动与报名，
 * 完全关闭后再启动一次，重新登录并查询活动、报名、通知与统计。
 *
 * <p>不使用 @SpringBootTest，避免测试框架缓存上下文；两次 Spring 容器
 * 都由本测试显式创建和关闭，因此“停服—重启”是实际发生的。</p>
 */
class CampusEventRestartTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    @Timeout(120)
    void restartKeepsActivitiesRegistrationsAndMessages() throws Exception {
        Path database = tempDir.resolve("restart.db");
        long activityId;
        long registrationId;

        try (ConfigurableApplicationContext first = start(database)) {
            String base = baseUrl(first);
            HttpClient admin = newClient();
            login(admin, base, "admin");

            String body = JSON.writeValueAsString(Map.of(
                    "title", "C10 重启验收活动",
                    "category", "验收测试",
                    "startAt", LocalDateTime.now().plusDays(10).withNano(0)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "location", "测试教室",
                    "capacity", 2,
                    "policyType", "DIRECT"
            ));
            HttpResponse<String> created = send(admin, "POST", base + "/api/admin/activities", body);
            assertEquals(200, created.statusCode(), created.body());
            activityId = JSON.readTree(created.body()).path("data").path("id").asLong();

            HttpClient student = newClient();
            login(student, base, "student");
            HttpResponse<String> registered = send(student, "POST",
                    base + "/api/student/activities/" + activityId + "/registrations", "");
            assertEquals(200, registered.statusCode(), registered.body());
            assertEquals("APPROVED", JSON.readTree(registered.body()).path("data").path("status").asText());
            registrationId = JSON.readTree(registered.body()).path("data").path("id").asLong();
        }

        assertTrue(Files.isRegularFile(database), "重启前数据库文件应存在");

        try (ConfigurableApplicationContext second = start(database)) {
            String base = baseUrl(second);

            HttpClient admin = newClient();
            login(admin, base, "admin");
            HttpResponse<String> activity = send(admin, "GET",
                    base + "/api/admin/activities/" + activityId, null);
            assertEquals(200, activity.statusCode(), activity.body());
            assertEquals("C10 重启验收活动",
                    JSON.readTree(activity.body()).path("data").path("title").asText());

            HttpResponse<String> statistics = send(admin, "GET", base + "/api/admin/statistics", null);
            assertEquals(200, statistics.statusCode(), statistics.body());
            JsonNode stats = JSON.readTree(statistics.body()).path("data");
            boolean found = false;
            for (JsonNode item : stats) {
                if (item.path("activityId").asLong() == activityId) {
                    found = true;
                    assertEquals(1, item.path("approvedCount").asInt(), "重启后已通过人数应保留");
                }
            }
            assertTrue(found, "统计中应包含重启前创建的活动");

            HttpClient student = newClient();
            login(student, base, "student");
            HttpResponse<String> registrations = send(student, "GET", base + "/api/student/registrations", null);
            assertEquals(200, registrations.statusCode(), registrations.body());
            boolean registrationFound = false;
            for (JsonNode item : JSON.readTree(registrations.body()).path("data")) {
                if (item.path("id").asLong() == registrationId) {
                    registrationFound = true;
                    assertEquals("APPROVED", item.path("status").asText());
                    assertEquals(activityId, item.path("activityId").asLong());
                }
            }
            assertTrue(registrationFound, "重启后报名记录应保留");

            HttpResponse<String> messages = send(student, "GET", base + "/api/student/messages", null);
            assertEquals(200, messages.statusCode(), messages.body());
            boolean messageFound = false;
            for (JsonNode item : JSON.readTree(messages.body()).path("data")) {
                if (item.path("activityId").asLong() == activityId
                        && item.path("content").asText().contains("报名成功")) {
                    messageFound = true;
                }
            }
            assertTrue(messageFound, "重启后站内通知应保留");
        }
    }

    private ConfigurableApplicationContext start(Path database) {
        return new SpringApplicationBuilder(CampusEventApplication.class)
                .run(
                        "--server.port=0",
                        "--CAMPUSEVENT_DB_PATH=" + database.toAbsolutePath(),
                        "--app.seed-demo-data=false",
                        "--spring.main.banner-mode=off",
                        "--logging.level.root=warn"
                );
    }

    private String baseUrl(ConfigurableApplicationContext context) {
        int port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();
        return "http://127.0.0.1:" + port;
    }

    private HttpClient newClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    private void login(HttpClient client, String base, String username) throws Exception {
        HttpResponse<String> response = send(client, "POST", base + "/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}");
        assertEquals(200, response.statusCode(), response.body());
    }

    private HttpResponse<String> send(HttpClient client, String method, String url, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json");
        if ("GET".equals(method)) {
            builder.GET();
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}