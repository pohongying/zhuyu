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

import java.util.List;
import java.util.Objects;

/**
 * 群消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo sender = recvInfo.getSender();
        List<IMUserInfo> receivers = recvInfo.getReceivers();
        log.info("接收到群消息，发送者:{},接收用户数量:{}，内容:{}", sender.getId(), receivers.size(), recvInfo.getData());
        for (IMUserInfo receiver : receivers) {
            try {
                ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
                if (!Objects.isNull(channelCtx)) {
                    // 推送消息到用户
                    IMSendInfo sendInfo = new IMSendInfo();
                    sendInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    // TODO: 2024/8/27  发送消息
                    channelCtx.channel().writeAndFlush(sendInfo);
                    // 消息发送成功确认
                    sendResult(recvInfo, receiver, IMSendCode.SUCCESS);

                } else {
                    // 未能找到对方的channel
                    sendResult(recvInfo, receiver, IMSendCode.NOT_FIND_CHANNEL);
                    log.error("未找到channel,发送者:{},接收id:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData());
                }
            } catch (Exception e) {
                // 未知异常
                sendResult(recvInfo, receiver, IMSendCode.UNKONW_ERROR);
                log.error("发送消息异常,发送者:{},接收id:{}，内容:{}", sender.getId(), receiver.getId(), recvInfo.getData());
            }
        }
    }


    private void sendResult(IMRecvInfo recvInfo, IMUserInfo receiver, IMSendCode sendCode) {
        if (recvInfo.getSendResult()) {
            IMSendResult result = new IMSendResult();
            result.setSender(recvInfo.getSender());
            result.setReceiver(receiver);
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":",IMRedisKey.IM_RESULT_GROUP_QUEUE,recvInfo.getServiceName());
            redisTemplate.opsForList().rightPush(key, result);
        }
    }
}
