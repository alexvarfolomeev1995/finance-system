package com.company.finance.startuplogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

/**
 * Слушает событие ApplicationReadyEvent и логирует информацию о конфигурации приложения.
 * Подключается автоматически через StartupLoggerAutoConfiguration, если свойство
 * app.startup-logger.enabled не false.
 */
public class StartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment environment;
    private final DataSource dataSource;
    private final Instant contextStartTime;

    public StartupLogger(Environment environment, DataSource dataSource, Instant contextStartTime) {
        this.environment = environment;
        this.dataSource = dataSource;
        this.contextStartTime = contextStartTime;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";

        String appName = environment.getProperty("spring.application.name", "unknown");
        String appVersion = environment.getProperty("spring.application.version", "n/a");
        String springBootVersion = environment.getProperty("spring-boot.version", "n/a");

        Duration startupDuration = Duration.between(contextStartTime, Instant.now());

        String jdbcUrl = maskJdbcUrl(resolveJdbcUrl());

        log.info("========================================================");
        log.info("  Startup Configuration Summary");
        log.info("========================================================");
        log.info("  Application      : {} v{}", appName, appVersion);
        log.info("  Active profiles  : {}", profiles);
        log.info("  Spring Boot      : {}", springBootVersion);
        log.info("  Database URL     : {}", jdbcUrl);
        log.info("  Context startup  : {} ms", startupDuration.toMillis());
        log.info("  Current time     : {}", Instant.now());
        log.info("========================================================");
    }

    private String resolveJdbcUrl() {

        String url = environment.getProperty("spring.datasource.url");
        if (url != null) {
            return url;
        }
        try (Connection conn = dataSource.getConnection()) {
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            return "unavailable (" + e.getMessage() + ")";
        }
    }

    private String maskJdbcUrl(String url) {
        if (url == null) return "n/a";
        return url.replaceAll("://[^/@]+@", "://");
    }
}