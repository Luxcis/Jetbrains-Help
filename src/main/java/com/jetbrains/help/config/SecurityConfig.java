package com.jetbrains.help.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Zhuang
 * @since 2024/6/13
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "help", name = "enable-auth")
public class SecurityConfig implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("启用授权功能......");
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptorExtend(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/**/*.html",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.svg",
                        "/**/*.png"
                );
    }
}
