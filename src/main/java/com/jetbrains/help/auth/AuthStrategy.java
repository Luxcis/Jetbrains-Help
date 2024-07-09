package com.jetbrains.help.auth;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @author Zhuang
 * @since 2024/7/9
 */
public interface AuthStrategy {
    /**
     * 身份验证
     *
     * @param params 参数
     * @return {@link String } 用户ID
     * @throws Exception 登录失败错误信息
     */
    String auth(Map<String, String> params) throws Exception;

    /**
     * 重定向到登录页面
     *
     * @param resp 请求响应，用于重定向
     */
    void redirect(HttpServletResponse resp) throws IOException;
}
