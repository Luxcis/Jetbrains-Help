package com.jetbrains.help.auth;

import cn.hutool.core.lang.UUID;
import com.jetbrains.help.properties.AuthProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Zhuang
 * @since 2024/7/9
 */
@Component("simple")
@RequiredArgsConstructor
public class SimpleAuth implements AuthStrategy {
    private final AuthProperties authProperties;

    @Override
    public String auth(@RequestParam Map<String, String> params) throws Exception {
        if (authProperties.get("code").equals(params.get("code"))) {
            return UUID.randomUUID().toString(true);
        }
        throw new Exception("密码错误");
    }

    @Override
    public void redirect(HttpServletResponse resp) throws IOException {
        resp.sendRedirect("auth.html");
    }

    @Override
    public List<String> whitelistPage() {
        return Collections.singletonList("auth.html");
    }
}
