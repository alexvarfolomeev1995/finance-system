package com.company.finance.startuplogger;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.time.Instant;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(
        prefix = "app.startup-logger",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(StartupLoggerProperties.class)
public class StartupLoggerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StartupLogger startupLogger(Environment environment, DataSource dataSource) {
        return new StartupLogger(environment, dataSource, Instant.now());
    }
}