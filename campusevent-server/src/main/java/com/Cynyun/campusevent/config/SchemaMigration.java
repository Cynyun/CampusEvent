package com.Cynyun.campusevent.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * V1 的 student 表没有 grade 列；change-v2 需要在旧库上补列。
 * schema 的 CREATE TABLE IF NOT EXISTS 不会修改已有表，因此这里做一次幂等迁移。
 * 优先于 DemoDataSeeder 执行，保证演示年级写入前列已存在。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (hasGradeColumn()) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE student ADD COLUMN grade INTEGER");
        log.info("旧库迁移完成：student 表已补充 grade 列");
    }

    private boolean hasGradeColumn() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(grade) FROM student", Integer.class);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
    }
}
