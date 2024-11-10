package com.luoyi.imserver.netty.processor;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.luoyi.imcommon.contant.IMConstant;
import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMCmdType;
import com.luoyi.imcommon.model.IMLoginInfo;
import com.luoyi.imcommon.model.IMSendInfo;
import com.luoyi.imcommon.model.IMSessionInfo;
import com.luoyi.imcommon.util.JwtUtil;
import com.luoyi.imcommon.contant.ChannelAttrKey;
import com.luoyi.imserver.netty.IMServerGroup;
import com.luoyi.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 登录处理器
 * 1. token检验
 * 2. 异地登录处理
 * 3. 用户与 Channel 绑定：
 * 4. 心跳初始化
 * 5. Redis 在线状态记录：
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginProcessor extends AbstractMessageProcessor<IMLoginInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.accessToken.secret}")
    private String accessTokenSecret;

    @Override
    public synchronized void process(ChannelHandlerContext ctx, IMLoginInfo loginInfo) {
        if (!JwtUtil.checkSign(loginInfo.getAccessToken(), accessTokenSecret)) {
            ctx.channel().close();
            log.warn("用户token校验不通过，强制下线,token:{}", loginInfo.getAccessToken());
        }
        String strInfo = JwtUtil.getInfo(loginInfo.getAccessToken());
        IMSessionInfo sessionInfo = JSON.parseObject(strInfo, IMSessionInfo.class);
        Long userId = sessionInfo.getUserId();
        Integer terminal = sessionInfo.getTerminal();
        log.info("用户登录，userId:{}", userId);
        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);
        if (context != null && !ctx.channel().id().equals(context.channel().id())) {
            // 不允许多地登录,强制下线
            IMSendInfo<Object> sendInfo = new IMSendInfo<>();
            sendInfo.setCmd(IMCmdType.FORCE_LOGUT.code());
            sendInfo.setData("您已在其他地方登陆，将被强制下线");
            context.channel().writeAndFlush(sendInfo);
            log.info("异地登录，强制下线,userId:{}", userId);
        }
        // 绑定用户和channel
        UserChannelCtxMap.addChannelCtx(userId, terminal, ctx);

        // 设置用户id属性 todo 这个很重要！ 为通道设置属性，方便后续获取
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        ctx.channel().attr(userIdAttr).set(userId);
        // 设置用户终端类型
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        ctx.channel().attr(terminalAttr).set(terminal);
        // 初始化心跳次数
        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf("HEARTBEAt_TIMES");
        ctx.channel().attr(heartBeatAttr).set(0L);

        // 在redis上记录每个user的channelId，15秒没有心跳，则自动过期
        String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), terminal.toString());

        redisTemplate.opsForValue().set(key, IMServerGroup.serverId, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        // 响应ws
        IMSendInfo<Object> sendInfo = new IMSendInfo<>();
        sendInfo.setCmd(IMCmdType.LOGIN.code());
        ctx.channel().writeAndFlush(sendInfo);  // TODO: 2024/8/27  
    }


    @Override
    public IMLoginInfo transForm(Object o) {
        HashMap map = (HashMap) o;
        return BeanUtil.fillBeanWithMap(map, new IMLoginInfo(), false);
    }
}
