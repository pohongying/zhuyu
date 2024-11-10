package com.luoyi.imserver.netty.ws.endecode;

import com.luoyi.imcommon.model.IMSendInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

/**
 * 消息解码器
 */
public class MessageProtocolDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame, List<Object> list) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        IMSendInfo sendInfo = objectMapper.readValue(textWebSocketFrame.text(), IMSendInfo.class);
        list.add(sendInfo);
    }
}
