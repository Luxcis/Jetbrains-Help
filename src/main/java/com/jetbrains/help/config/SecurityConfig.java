package com.jetbrains.help.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jetbrains.help.auth.AuthStrategy;
import com.jetbrains.help.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auth", name = "enable")
public class SecurityConfig implements WebMvcConfigurer {
    private final AuthProperties authProperties;

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("启用{}授权功能......", authProperties.getType());
        AuthStrategy auth = SpringUtil.getBean(authProperties.getType(), AuthStrategy.class);
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptorExtend(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/**/auth.html",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.svg",
                        "/**/*.png"
                )
                .excludePathPatterns(auth.whitelistPage());
    }
}
