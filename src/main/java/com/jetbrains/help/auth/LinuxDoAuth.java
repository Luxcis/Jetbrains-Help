package com.jetbrains.help.auth;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.jetbrains.help.properties.AuthProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author Zhuang
 * @since 2024/7/9
 */
@Slf4j
@Component("linuxDo")
@RequiredArgsConstructor
public class LinuxDoAuth implements AuthStrategy {
    private static final String base = "https://connect.linux.do";
    private static final String authorize = base + "/oauth2/authorize";
    private static final String token = base + "/oauth2/token";
    private static final String user = base + "/api/user";
    private final AuthProperties properties;

    @Override
    public String auth(Map<String, String> params) throws Exception {
        LinuxDoUser user = this.user(this.token(params.get("code")));
        return user.getId();
    }

    @Override
    public void redirect(HttpServletResponse resp) throws IOException {
        String state = RandomUtil.randomString(10);
        String url = StrUtil.format("{}?client_id={}&response_type=code&scope=user:email&state={}", authorize, properties.get("clientId"), state);
        resp.sendRedirect(url);
    }

    private OauthToken token(String code) {
        String secret = Base64.encode(properties.get("clientId") + ":" + properties.get("clientSecret"));
        log.debug("code: {}", code);
        log.debug("secret: {}", secret);
        String body;
        try (HttpResponse response = HttpUtil.createPost(token)
                .auth("Basic " + secret)
                .body(StrUtil.format("grant_type=authorization_code&code={}&redirect_uri{}", code, properties.get("redirectUri")))
                .execute()) {
            body = response.body();
        }
        return JSONUtil.toBean(body, OauthToken.class);
    }

    private LinuxDoUser user(OauthToken token) {
        log.debug("token: {}", token);
        String body;
        try (HttpResponse response = HttpUtil.createGet(user)
                .bearerAuth(token.getAccess_token())
                .execute()) {
            body = response.body();
        }
        return JSONUtil.toBean(body, LinuxDoUser.class);
    }

    @Data
    public static class OauthCode {
        private String code;
        private String state;
    }

    @Data
    public static class OauthToken {
        private String access_token;
        private String token_type;
        private String scope;
        private String refresh_token;
    }

    @Data
    public static class LinuxDoUser {
        private String id;
        private String username;
        private String name;
        private String active;
        private String trust_level;
        private String silenced;
    }
}
