package com.luoyi.implatform.aspect;

import com.luoyi.imclient.IMClient;
import com.luoyi.implatform.exception.GlobalException;
import com.luoyi.implatform.session.SessionContext;
import com.luoyi.implatform.session.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 实现注解，用于检查用户是否在线
 */

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor // 表示类中的final成员必须在构造函数中初始化

public class OnlineCheckAspect {

    // IM客户端，用于检查用户是否在线
    private final IMClient imClient;

    /**
     * 在标注了@OnlineCheck注解的方法执行前后执行
     * 主要用于检查用户网络连接是否在线
     *
     * @param joinPoint 切入点对象，表示正在执行的方法
     * @return 方法执行结果
     * @throws Throwable 如果用户不在线，抛出异常
     */
    @Around("@annotation(com.luoyi.imcommon.annotation.OnlineCheck)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取用户会话
        UserSession session = SessionContext.getSession();
        // 检查用户是否在线
        if(!imClient.isOnline(session.getUserId())){
            // 如果不在线，抛出全局异常
            throw new GlobalException("您当前的网络连接已断开,请稍后重试");
        }
        // 继续执行原方法
        return joinPoint.proceed();
    }

}

