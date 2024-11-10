package com.luoyi.imserver.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类主要用于在Netty服务器中管理用户与他们的网络连接（通道）之间的关系，
 * 方便根据用户和终端类型快速查找或修改连接状态。
 */
public class UserChannelCtxMap {

    /**
     *  维护userId和ctx的关联关系，格式:Map<userId,map<terminal，ctx>>
     */
    private static Map<Long, Map<Integer, ChannelHandlerContext>> channelMap = new ConcurrentHashMap();

    public static void addChannelCtx(Long userId, Integer channel, ChannelHandlerContext ctx) {
        channelMap.computeIfAbsent(userId, key -> new ConcurrentHashMap()).put(channel, ctx);
    }

    public static void removeChannelCtx(Long userId, Integer terminal) {
        if (userId != null && terminal != null && channelMap.containsKey(userId)) {
            Map<Integer, ChannelHandlerContext> userChannelMap = channelMap.get(userId);
            userChannelMap.remove(terminal);
            if (userChannelMap.isEmpty()) {
                channelMap.remove(userId);
            }
        }
    }

    public static ChannelHandlerContext getChannelCtx(Long userId, Integer terminal) {
        if (userId != null && terminal != null && channelMap.containsKey(userId)) {
            Map<Integer, ChannelHandlerContext> userChannelMap = channelMap.get(userId);
            if (userChannelMap.containsKey(terminal)) {
                return userChannelMap.get(terminal);
            }
        }
        return null;
    }

    public static Map<Integer, ChannelHandlerContext> getChannelCtx(Long userId) {
        if (userId == null) {
            return null;
        }
        return channelMap.get(userId);
    }

}
