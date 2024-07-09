package com.jetbrains.help.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.extra.spring.SpringUtil;
import com.jetbrains.help.auth.AuthStrategy;
import com.jetbrains.help.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Zhuang
 * @since 2024/7/9
 */
@SaIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {
    private final AuthProperties authProperties;

    @PostMapping
    public SaResult auth(@RequestParam Map<String, String> params) {
        AuthStrategy auth = SpringUtil.getBean(authProperties.getType(), AuthStrategy.class);
        try {
            String id = auth.auth(params);
            StpUtil.login(id);
            return SaResult.ok();
        } catch (Exception e) {
            return SaResult.error(e.getMessage());
        }
    }

    @RequestMapping("callback")
    public SaResult callback(@RequestParam Map<String, String> params) {
        return auth(params);
    }
}
