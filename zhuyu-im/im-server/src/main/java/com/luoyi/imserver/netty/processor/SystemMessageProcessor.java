package com.luoyi.imserver.netty.processor;

import cn.hutool.core.util.StrUtil;
import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMCmdType;
import com.luoyi.imcommon.enums.IMSendCode;
import com.luoyi.imcommon.model.IMRecvInfo;
import com.luoyi.imcommon.model.IMSendInfo;
import com.luoyi.imcommon.model.IMSendResult;
import com.luoyi.imcommon.model.IMUserInfo;
import com.luoyi.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 系统消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo receiver = recvInfo.getReceivers().get(0); // todo 因为接收消息时，系统消息只有一个接收者
        log.info("接收到系统消息,接收者:{}，内容:{}",  receiver.getId(), recvInfo.getData());
        try {
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
            if (!Objects.isNull(channelCtx)) {
                // 推送消息到用户
                IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                sendInfo.setCmd(IMCmdType.SYSTEM_MESSAGE.code());
                sendInfo.setData(recvInfo.getData());
                // TODO: 2024/8/27 发送消息给客户端
                channelCtx.channel().writeAndFlush(sendInfo);
                // 消息发送成功确认
                sendResult(recvInfo, IMSendCode.SUCCESS);
            } else {
                // 消息推送失败确认
                sendResult(recvInfo, IMSendCode.NOT_FIND_CHANNEL);
                log.error("未找到channel，接收者:{}，内容:{}",receiver.getId(), recvInfo.getData());
            }
        } catch (Exception e) {
            // 消息推送失败确认
            sendResult(recvInfo, IMSendCode.UNKONW_ERROR);
            log.error("发送异常，,接收者:{}，内容:{}", receiver.getId(), recvInfo.getData(), e);
        }

    }

    private void sendResult(IMRecvInfo recvInfo, IMSendCode sendCode) {
        if (recvInfo.getSendResult()) {
            IMSendResult<Object> result = new IMSendResult<>();
            result.setReceiver(recvInfo.getReceivers().get(0));
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":",IMRedisKey.IM_RESULT_SYSTEM_QUEUE,recvInfo.getServiceName());
            redisTemplate.opsForList().rightPush(key, result);
        }
    }
}
