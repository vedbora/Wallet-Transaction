package com.example.wallet;

import com.example.wallet.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class WalletTransactionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletTransactionSystemApplication.class, args);
    }
}

