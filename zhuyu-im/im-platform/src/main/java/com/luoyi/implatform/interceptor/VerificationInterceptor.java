/*
package com.bx.implatform.interceptor;

import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


*/
/**
 * @Description Speak less ,type more code
 * @Author Luoyi
 * @Date 2024/8/29
 * <p>
 * 验证码拦截器
 *//*

@Component
@Slf4j
public class VerificationInterceptor implements HandlerInterceptor {

    */
/**
     * @param request
     * @param response
     * @param handler
     * @return 表示允许请求继续传递到下一个拦截器或目标处理器
     * @throws Exception
     *//*

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("POST".equals(request.getMethod()) && "/login".equals(request.getRequestURI())) {
            System.out.println("用户名"+request.getParameter("userName"));
            System.out.println("密码"+request.getParameter("password"));
            String code = request.getParameter("code");
            String verify_code = (String) request.getSession().getAttribute("verify_code");

            if (verify_code == null || !code.equalsIgnoreCase(verify_code)) {
                throw new GlobalException(ResultCode.VERIFICATION_CODE_ERROR, "验证码错误");
            }
        }

        return true;
    }
}*/
