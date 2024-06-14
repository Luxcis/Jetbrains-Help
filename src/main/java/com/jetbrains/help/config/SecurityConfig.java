package com.jetbrains.help.config;

import com.jetbrains.help.properties.JetbrainsHelpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JetbrainsHelpProperties helper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
                .formLogin(formLogin -> formLogin.loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/loginError")
                        .permitAll());
        if (helper.getEnableSecurity()) {
            http.authorizeHttpRequests(req -> req.requestMatchers("/static/**").permitAll()
                    .anyRequest().authenticated());
        } else {
            http.authorizeHttpRequests(req -> req.anyRequest().permitAll());
        }
        return http.build();
    }
}
