package com.luoyi.imserver.netty.processor;

import cn.hutool.core.bean.BeanUtil;
import com.luoyi.imcommon.contant.IMConstant;
import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMCmdType;
import com.luoyi.imcommon.model.IMHeartbeatInfo;
import com.luoyi.imcommon.model.IMSendInfo;
import com.luoyi.imcommon.contant.ChannelAttrKey;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 心跳处理器
 * 1. 心跳响应ws
 * 2. 心跳次数自增
 * 3. 延长心跳时间
 * 4. 日志记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatProcessor extends AbstractMessageProcessor<IMHeartbeatInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(ChannelHandlerContext ctx, IMHeartbeatInfo beatInfo) {
        // 响应ws
        IMSendInfo sendInfo = new IMSendInfo();
        sendInfo.setCmd(IMCmdType.HEART_BEAT.code());
        ctx.channel().writeAndFlush(sendInfo); // 将心跳发送给客户端

        // todo 存储和检索自定义数据 设置属性
        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf(ChannelAttrKey.HEARTBEAT_TIMES);

        Long heartbeatTimes = ctx.channel().attr(heartBeatAttr).get();
        ctx.channel().attr(heartBeatAttr).set(++heartbeatTimes);  // todo 心跳次数自增
        if (heartbeatTimes % 10 == 0) {
            // 每心跳10次，用户在线状态续一次命
            AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
            Long userId = ctx.channel().attr(userIdAttr).get();
            AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
            Integer terminal = ctx.channel().attr(terminalAttr).get();
            String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
            // 重新设置过期时间
            redisTemplate.expire(key, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        }
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        Long userId = ctx.channel().attr(userIdAttr).get();
        log.info("心跳,userId:{},{}",userId,ctx.channel().id().asLongText());
    }

    /**
     * 消息对象转换为IMHeartbeatInfo对象
     * @param o
     * @return
     */
    @Override
    public IMHeartbeatInfo transForm(Object o) {
        HashMap map = (HashMap) o;
        return BeanUtil.fillBeanWithMap(map, new IMHeartbeatInfo(), false);
    }
}
