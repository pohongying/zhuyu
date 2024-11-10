package com.luoyi.implatform.session;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


public class SessionContext {

    /**
     获取session
     * @return: com.bx.implatform.session.UserSession
     * @author: luoyi
     * @date: 2024/8/27 10:35
     */
    public static UserSession getSession() {
        // 从请求上下文里获取Request对象
        // RequestContextHolder是一个工具类，可以获取当前请求的Request对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return (UserSession) request.getAttribute("session");
    }

}
