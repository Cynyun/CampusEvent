package com.Cynyun.campusevent.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 首次启动时初始化可演示的数据：
 * - 演示账号 student / admin；
 * - 8 个未来活动（直接报名与审核制混合）；
 * - 36 个学生账号（student01 - student36）；
 * - 100 条左右的报名记录及对应站内通知。
 *
 * 幂等性由 app_meta.demo_seeded 保证：已有业务数据时只写入标记，不重复插入。
 * 可用 CAMPUSEVENT_SEED_DEMO=false 关闭演示数据（演示账号仍会创建）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private static final String DEMO_PASSWORD_HASH =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"; // 123456
    private static final String SEED_MARKER = "demo_seeded";
    private static final int STUDENT_COUNT = 36;

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureAccount("student", "演示学生", DEMO_PASSWORD_HASH, true);
        ensureAccount("admin", "系统管理员", DEMO_PASSWORD_HASH, false);
        ensureDemoGrades();

        if (!seedDemoData) {
            log.info("演示数据初始化已关闭（app.seed-demo-data=false），仅确保演示账号存在");
            return;
        }
        if (hasSeedMarker()) {
            log.info("演示数据已初始化过，跳过");
            return;
        }

        Integer existingActivities = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM activity", Integer.class);
        if (existingActivities != null && existingActivities > 0) {
            markSeeded("existing-data");
            log.info("检测到已有 {} 条活动，保留现有数据，不重复写入演示数据", existingActivities);
            return;
        }

        List<Long> activityIds = seedActivities();
        int studentCount = seedStudents();
        ensureDemoGrades();
        int registrationCount = seedRegistrations(activityIds);
        markSeeded("v1");

        log.info("首次运行初始化完成：{} 个活动，{} 个学生账号，{} 条报名记录",
                activityIds.size(), studentCount, registrationCount);
    }

    private void ensureAccount(String id, String name, String hash, boolean student) {
        String table = student ? "student" : "admin";
        String idColumn = student ? "student_id" : "admin_id";
        jdbcTemplate.update(
                "INSERT INTO " + table + " (" + idColumn + ", name, password_hash) " +
                        "SELECT ?, ?, ? WHERE NOT EXISTS " +
                        "(SELECT 1 FROM " + table + " WHERE " + idColumn + " = ?)",
                id, name, hash, id);
    }

    /**
     * 只为演示账号补年级；已有非空 grade 不覆盖，避免覆盖真实数据。
     * student 固定为 2（大二）；student01—student04 分别 1—4，其余按 1—4 循环。
     */
    private void ensureDemoGrades() {
        setGradeIfNull("student", 2);
        for (int i = 1; i <= STUDENT_COUNT; i++) {
            setGradeIfNull(String.format("student%02d", i), gradeForStudent(i));
        }
    }

    private void setGradeIfNull(String studentId, int grade) {
        jdbcTemplate.update(
                "UPDATE student SET grade = ? WHERE student_id = ? AND grade IS NULL",
                grade, studentId);
    }

    private int gradeForStudent(int index) {
        if (index <= 4) {
            return index;
        }
        return ((index - 1) % 4) + 1;
    }

    private boolean hasSeedMarker() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_meta WHERE meta_key = ?", Integer.class, SEED_MARKER);
        return count != null && count > 0;
    }

    private void markSeeded(String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_meta WHERE meta_key = ?", Integer.class, SEED_MARKER);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO app_meta (meta_key, meta_value) VALUES (?, ?)", SEED_MARKER, value);
        }
    }

    private List<Long> seedActivities() {
        List<SeedActivity> activities = List.of(
                new SeedActivity("校园开放日志愿者", "志愿服务", "DIRECT", 30,
                        LocalDate.now().plusDays(5), LocalTime.of(9, 0), "大学生活动中心 301"),
                new SeedActivity("人工智能前沿讲座", "学术讲座", "APPROVAL", 60,
                        LocalDate.now().plusDays(7), LocalTime.of(14, 0), "图书馆报告厅"),
                new SeedActivity("冬季校园马拉松", "文体活动", "DIRECT", 100,
                        LocalDate.now().plusDays(10), LocalTime.of(8, 0), "东区操场"),
                new SeedActivity("创业计划工作坊", "创新创业", "APPROVAL", 40,
                        LocalDate.now().plusDays(13), LocalTime.of(19, 0), "创新创业学院 204"),
                new SeedActivity("校园摄影采风", "文体活动", "DIRECT", 25,
                        LocalDate.now().plusDays(16), LocalTime.of(9, 30), "老校区银杏大道"),
                new SeedActivity("读书分享会", "学术讲座", "APPROVAL", 35,
                        LocalDate.now().plusDays(18), LocalTime.of(19, 0), "图书馆研讨室 2"),
                new SeedActivity("名企校园招聘宣讲", "创新创业", "DIRECT", 80,
                        LocalDate.now().plusDays(20), LocalTime.of(14, 0), "大学生活动中心报告厅"),
                new SeedActivity("公益夜跑", "志愿服务", "DIRECT", 60,
                        LocalDate.now().plusDays(23), LocalTime.of(19, 30), "西区田径场")
        );

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Long> ids = new ArrayList<>();
        for (SeedActivity activity : activities) {
            LocalDateTime startAt = activity.startDate().atTime(activity.startTime());
            jdbcTemplate.update(
                    "INSERT INTO activity (title, category, start_at, location, capacity, " +
                            "policy_type, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    activity.title(), activity.category(), Timestamp.valueOf(startAt),
                    activity.location(), activity.capacity(), activity.policyType(),
                    Timestamp.valueOf(now), Timestamp.valueOf(now));
            Long id = jdbcTemplate.queryForObject(
                    "SELECT id FROM activity WHERE title = ? ORDER BY id DESC LIMIT 1",
                    Long.class, activity.title());
            ids.add(id);
        }
        return ids;
    }

    private int seedStudents() {
        int created = 0;
        for (int i = 1; i <= STUDENT_COUNT; i++) {
            String id = String.format("student%02d", i);
            String name = String.format("演示学生%02d", i);
            ensureAccount(id, name, DEMO_PASSWORD_HASH, true);
            created++;
        }
        return created;
    }

    private int seedRegistrations(List<Long> activityIds) {
        List<SeedRegistration> registrations = new ArrayList<>();
        // 活动 0：直接报名，1-20 号通过
        addRange(registrations, activityIds.get(0), 1, 20, "APPROVED", "校园开放日志愿者");
        // 活动 1：审核制，1-15 号通过，16-22 号待审核
        addRange(registrations, activityIds.get(1), 1, 15, "APPROVED", "人工智能前沿讲座");
        addRange(registrations, activityIds.get(1), 16, 22, "PENDING", "人工智能前沿讲座");
        // 活动 2：直接报名，21-36 号通过
        addRange(registrations, activityIds.get(2), 21, 36, "APPROVED", "冬季校园马拉松");
        // 活动 3：审核制，10-20 号待审核
        addRange(registrations, activityIds.get(3), 10, 20, "PENDING", "创业计划工作坊");
        // 活动 4：直接报名，5-18 号通过
        addRange(registrations, activityIds.get(4), 5, 18, "APPROVED", "校园摄影采风");
        // 活动 5：审核制，20-30 号待审核
        addRange(registrations, activityIds.get(5), 20, 30, "PENDING", "读书分享会");
        // 活动 6：直接报名，1-10 号通过
        addRange(registrations, activityIds.get(6), 1, 10, "APPROVED", "名企校园招聘宣讲");
        // 活动 7：直接报名，25-36 号通过
        addRange(registrations, activityIds.get(7), 25, 36, "APPROVED", "公益夜跑");

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        for (SeedRegistration registration : registrations) {
            jdbcTemplate.update(
                    "INSERT INTO registration (student_id, activity_id, status, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    registration.studentId(), registration.activityId(), registration.status(),
                    Timestamp.valueOf(now), Timestamp.valueOf(now));
            String content = "APPROVED".equals(registration.status())
                    ? "报名成功：《" + registration.activityTitle() + "》"
                    : "报名已提交，等待审核：《" + registration.activityTitle() + "》";
            jdbcTemplate.update(
                    "INSERT INTO message (student_id, activity_id, content, created_at, is_read) " +
                            "VALUES (?, ?, ?, ?, 0)",
                    registration.studentId(), registration.activityId(), content, Timestamp.valueOf(now));
        }
        return registrations.size();
    }

    private void addRange(List<SeedRegistration> target, Long activityId, int from, int to,
                          String status, String activityTitle) {
        for (int i = from; i <= to; i++) {
            target.add(new SeedRegistration(
                    String.format("student%02d", i), activityId, status, activityTitle));
        }
    }

    private record SeedActivity(String title, String category, String policyType, int capacity,
                                LocalDate startDate, LocalTime startTime, String location) {
    }

    private record SeedRegistration(String studentId, Long activityId, String status,
                                    String activityTitle) {
    }
}
