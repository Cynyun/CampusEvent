package com.Cynyun.campusevent.config.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * SQLite JDBC 会把 Timestamp 存成 epoch 毫秒整数，而 MyBatis 默认的
 * LocalDateTimeTypeHandler 会调用 getObject(..., LocalDateTime.class)，导致解析失败。
 * 这里兼容 SQLite 的整数/字符串与 MySQL 的 Timestamp 返回值。
 */
@MappedTypes(LocalDateTime.class)
public class FlexibleLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final DateTimeFormatter SPACE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getObject(columnName));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getObject(columnIndex));
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getObject(columnIndex));
    }

    private LocalDateTime convert(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof Number number) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(number.longValue()), ZoneId.systemDefault());
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().atStartOfDay();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.matches("\\d+")) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Long.parseLong(text)), ZoneId.systemDefault());
        }
        if (text.contains("T")) {
            return LocalDateTime.parse(text);
        }
        return LocalDateTime.parse(text, SPACE_FORMAT);
    }
}
