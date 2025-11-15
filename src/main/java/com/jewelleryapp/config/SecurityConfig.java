package com.jewelleryapp.config;

import com.jewelleryapp.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 1. Auth & Public Tooling Endpoints
                        .requestMatchers(
                                "/api/v1/auth/**" // All auth endpoints
                        ).permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // 2. Public Read (GET) for all catalogue/product-related info
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/product-images/**",
                                "/api/v1/categories/**",
                                "/api/v1/collections/**",
                                "/api/v1/attribute-types/**",
                                "/api/v1/attribute-values/**",
                                "/api/v1/stock-items/**",
                                "/api/v1/stores/**"
                        ).permitAll()

                        // 3. Admin Write (POST, PUT, DELETE) for Core Catalogue
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products/**",
                                "/api/v1/product-images/**",
                                "/api/v1/categories/**",
                                "/api/v1/collections/**",
                                "/api/v1/attribute-types/**",
                                "/api/v1/attribute-values/**",
                                "/api/v1/stores/**"
                        ).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/products/**",
                                "/api/v1/product-images/**",
                                "/api/v1/categories/**",
                                "/api/v1/collections/**",
                                "/api/v1/attribute-types/**",
                                "/api/v1/attribute-values/**",
                                "/api/v1/stores/**"
                        ).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/products/**",
                                "/api/v1/product-images/**",
                                "/api/v1/categories/**",
                                "/api/v1/collections/**",
                                "/api/v1/attribute-types/**",
                                "/api/v1/attribute-values/**",
                                "/api/v1/stores/**"
                        ).hasAuthority("ADMIN")

                        // 4. Stock Management - Admin OR Store Manager
                        .requestMatchers(HttpMethod.POST, "/api/v1/stock-items/**").hasAnyAuthority("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/stock-items/**").hasAnyAuthority("ADMIN", "STORE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/stock-items/**").hasAnyAuthority("ADMIN", "STORE_MANAGER")

                        // 5. Admin-only User Management
                        .requestMatchers("/api/v1/users/**").hasAuthority("ADMIN")

                        // 6. All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Logout successful.\"}");
                        })
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Updated Allowed Origins for your Jewellery App
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173", // For local development
                "https://your-jewellery-frontend.com" // Placeholder for your production URL
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}