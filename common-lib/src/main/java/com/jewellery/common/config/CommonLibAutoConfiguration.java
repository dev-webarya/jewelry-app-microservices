package com.jewellery.common.config;

import com.jewellery.common.exception.GlobalExceptionHandler;
import com.jewellery.common.security.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for common library components
 * Import this in each microservice to get shared functionality
 */
@Configuration
@Import({ GlobalExceptionHandler.class })
public class CommonLibAutoConfiguration {

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }
}
