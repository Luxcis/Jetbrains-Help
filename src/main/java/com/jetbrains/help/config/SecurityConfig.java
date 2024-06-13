package com.jetbrains.help.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Zhuang
 * @since 2024/6/13
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
                .formLogin(formLogin -> formLogin.loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/loginError")
                        .permitAll())
                .authorizeHttpRequests(req -> req.requestMatchers("/static/**").permitAll()
                        .anyRequest().authenticated())
                .build();
    }
}
