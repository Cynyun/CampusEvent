package com.Cynyun.campusevent.config;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 让同一份 Mapper XML 在 SQLite 与 MySQL 下走不同 SQL：
 * - MySQL 使用 SELECT ... FOR UPDATE 锁行；
 * - SQLite 不支持 FOR UPDATE，使用普通 SELECT，由单连接池串行化写入。
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("SQLite", "sqlite");
        provider.setProperties(properties);
        return provider;
    }
}
