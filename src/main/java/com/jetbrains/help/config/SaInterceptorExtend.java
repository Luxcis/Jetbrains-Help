package com.jetbrains.help.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.http.ContentType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * @author Zhuang
 * @since 2023/11/16
 */
@Slf4j
public class SaInterceptorExtend extends SaInterceptor {
    /**
     * 创建一个 Sa-Token 综合拦截器，默认带有注解鉴权能力
     *
     * @param auth 认证函数，每次请求执行
     */
    public SaInterceptorExtend(SaParamFunction<Object> auth) {
        this.auth = auth;
    }

    @Override
    @SuppressWarnings("all")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            if (request.getMethod().equals(cn.hutool.http.Method.OPTIONS.name())) {
                log.debug("[{}][{}]====预检请求直接放行", request.getMethod(), request.getServletPath());
                return true;
            }
            // 这里必须确保 handler 是 HandlerMethod 类型时，才能进行注解鉴权
            if (isAnnotation && handler instanceof HandlerMethod) {
                // 获取此请求对应的 Method 处理函数
                Method method = ((HandlerMethod) handler).getMethod();

                // 如果此 Method 或其所属 Class 标注了 @SaIgnore，则忽略掉鉴权
                if (SaStrategy.instance.isAnnotationPresent.apply(method, SaIgnore.class)) {
                    // 注意这里直接就退出整个鉴权了，最底部的 auth.run() 路由拦截鉴权也被跳出了
                    return true;
                }
                // 注解校验
                SaStrategy.instance.checkMethodAnnotation.accept(method);
            }
            // Auth 校验
            auth.run(handler);
        } catch (StopMatchException e) {
            // StopMatchException 异常代表：停止匹配，进入Controller
            log.debug("[{}][{}]====停止匹配，进入Controller", request.getMethod(), request.getServletPath());
        } catch (BackResultException e) {
            // BackResultException 异常代表：停止匹配，向前端输出结果
            // 		请注意此处默认 Content-Type 为 text/plain，如果需要返回 JSON 信息，需要在 back 前自行设置 Content-Type 为 application/json
            // 		例如：SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
            log.debug("[{}][{}]====停止匹配，向前端输出结果", request.getMethod(), request.getServletPath());
            response.setContentType("text/plain; charset=utf-8");
            response.getWriter().print(SaResult.error(e.getMessage()));
            return false;
        } catch (NotLoginException e) {
            log.debug("[{}][{}]====停止匹配，未登录用户", request.getMethod(), request.getServletPath());
            if (ContentType.JSON.getValue().equals(request.getContentType())) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print(new SaResult(401, e.getMessage(), null));
            } else {
                response.sendRedirect("auth.do");
            }
            return false;
        } catch (Exception e) {
            log.error("[{}][{}]====停止匹配，未知错误\n{}", request.getMethod(), request.getServletPath(), e);
            if (ContentType.JSON.getValue().equals(request.getContentType())) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.getWriter().print(SaResult.error(e.getMessage()));
            } else {
                response.sendRedirect("auth.do");
            }
            return false;
        }
        // 通过验证
        return true;
    }

}
