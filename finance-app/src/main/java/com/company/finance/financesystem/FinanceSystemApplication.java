package com.company.finance.financesystem;

import com.company.finance.financesystem.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FinanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceSystemApplication.class, args);
    }

}
