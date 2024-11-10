package com.luoyi.imclient.sender;

import cn.hutool.core.collection.CollUtil;
import com.luoyi.imclient.listener.MessageListenerMulticaster;
import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMCmdType;
import com.luoyi.imcommon.enums.IMListenerType;
import com.luoyi.imcommon.enums.IMSendCode;
import com.luoyi.imcommon.enums.IMTerminalType;
import com.luoyi.imcommon.model.*;
import com.luoyi.imcommon.mq.RedisMQTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class IMSender {

    @Autowired
    private RedisMQTemplate redisMQTemplate;

    @Value("${spring.application.name}")
    private String appName;

    private final MessageListenerMulticaster listenerMulticaster;


    /**
     * 发送系统消息
     *
     * @param message
     * @param <T>
     */
    public <T> void sendSystemMessage(IMSystemMessage<T> message) {
        // 根据群聊每个成员所连的IM-server，进行分组
        Map<String, IMUserInfo> sendMap = new HashMap<>();
        for (Integer terminal : message.getRecvTerminals()) {
            message.getRecvIds().forEach(id -> {
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                sendMap.put(key, new IMUserInfo(id, terminal));
            });
        }

        // todo 批量拉取服务id
        List<Object> serverIds = redisMQTemplate.opsForValue().multiGet(sendMap.keySet());

        // 格式:map<服务器id,list<接收方>>
        Map<Integer, List<IMUserInfo>> serverMap = new HashMap<>();
        List<IMUserInfo> offLineUsers = new LinkedList<>();

        int idx = 0;
        for (Map.Entry<String, IMUserInfo> entry : sendMap.entrySet()) {
            Integer serverId = (Integer) serverIds.get(idx++);
            if (!Objects.isNull(serverId)) {
                List<IMUserInfo> list = serverMap.computeIfAbsent(serverId, o -> new LinkedList<>());
                list.add(entry.getValue());
            } else {
                // 加入离线列表
                offLineUsers.add(entry.getValue());
            }
        }

        // 逐个server发送
        for (Map.Entry<Integer, List<IMUserInfo>> entry : serverMap.entrySet()) {
            IMRecvInfo recvInfo = new IMRecvInfo();
            recvInfo.setCmd(IMCmdType.SYSTEM_MESSAGE.code());
            recvInfo.setReceivers(new LinkedList<>(entry.getValue()));
            recvInfo.setServiceName(appName);
            recvInfo.setSendResult(message.getSendResult());
            recvInfo.setData(message.getData());

            // todo 推送至队列
            String key = String.join(":", IMRedisKey.IM_MESSAGE_SYSTEM_QUEUE, entry.getKey().toString());
            redisMQTemplate.opsForList().rightPush(key, recvInfo);
        }

        // 对离线用户回复消息状态
        if (message.getSendResult() && !offLineUsers.isEmpty()) {
            List<IMSendResult> results = new LinkedList<>();
            for (IMUserInfo offLineUser : offLineUsers) {
                IMSendResult result = new IMSendResult();
                result.setReceiver(offLineUser);
                result.setCode(IMSendCode.NOT_ONLINE.code());
                result.setData(message.getData());
                results.add(result);
            }
            listenerMulticaster.multicast(IMListenerType.SYSTEM_MESSAGE, results);
        }
    }


    /**
     * 发送私聊消息
     *
     * @param message
     * @param <T>
     */
    public <T> void sendPrivateMessage(IMPrivateMessage<T> message) {
        List<IMSendResult> results = new LinkedList<>();
        if (!Objects.isNull(message.getRecvId())) {
            for (Integer terminal : message.getRecvTerminals()) {
                // todo 获取对方连接的channelId
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, message.getRecvId().toString(), terminal.toString());
                Integer serverId = (Integer) redisMQTemplate.opsForValue().get(key);
                // 如果对方在线，将数据存储至redis，等待拉取推送
                if (serverId != null) {
                    String sendKey = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, serverId.toString());
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    recvInfo.setSendResult(message.getSendResult());
                    recvInfo.setServiceName(appName);
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getRecvId(), terminal)));
                    recvInfo.setData(message.getData());
                    redisMQTemplate.opsForList().rightPush(sendKey, recvInfo);   // list存储
                } else {
                    // 不在线，回复消息状态
                    IMSendResult result = new IMSendResult();
                    result.setSender(message.getSender());
                    result.setReceiver(new IMUserInfo(message.getRecvId(), terminal));
                    result.setCode(IMSendCode.NOT_ONLINE.code());
                    result.setData(message.getData());
                    results.add(result);
                }
            }
        }

        // 推送给自己的其他终端
        if (message.getSendToSelf()) {
            for (Integer terminal : IMTerminalType.codes()) {
                if (message.getSender().getTerminal().equals(terminal)) {
                    continue;
                }
                // 获取终端连接的channelId
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, message.getSender().getId().toString(), terminal.toString());
                Integer serverId = (Integer) redisMQTemplate.opsForValue().get(key);
                // 如果终端在线，将数据存储至redis，等待拉取推送
                if (serverId != null) {
                    String sendKey = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, serverId.toString());
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    // 自己的消息不需要回推消息结果
                    recvInfo.setSendResult(false);
                    recvInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getId(), terminal)));
                    recvInfo.setData(message.getData());
                    redisMQTemplate.opsForList().rightPush(sendKey, recvInfo);
                }
            }
        }


        // 对离线用户回复消息状态
        if (message.getSendResult() && !results.isEmpty()) {
            listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, results);
        }

    }

    /**
     * 群聊消息
     *
     * @param message
     * @param <T>
     */
    public <T> void sendGroupMessage(IMGroupMessage<T> message) {
        // 根据群聊每个成员所连的IM-server，进行分组
        Map<String, IMUserInfo> sendMap = new HashMap<>();
        for (Integer terminal : message.getRecvTerminals()) {
            message.getRecvIds().forEach(id -> {
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                sendMap.put(key, new IMUserInfo(id, terminal));
            });
        }
        // 批量拉取
        List<Object> serverIds = redisMQTemplate.opsForValue().multiGet(sendMap.keySet());

        // 在线用户格式:map<服务器id,list<接收方>>
        Map<Integer, List<IMUserInfo>> serverMap = new HashMap<>();
        // 离线用户
        List<IMUserInfo> offLineUsers = new LinkedList<>();

        int idx = 0;
        for (Map.Entry<String, IMUserInfo> entry : sendMap.entrySet()) {
            Integer serverId = (Integer) serverIds.get(idx++);

            if (!Objects.isNull(serverId)) { // 获取或创建一个与该服务器ID关联的用户列表，并将当前用户信息添加到这个列表中
                List<IMUserInfo> list = serverMap.computeIfAbsent(serverId, o -> new LinkedList<>());
                list.add(entry.getValue());
            } else {
                // 加入离线列表
                offLineUsers.add(entry.getValue());
            }
        }
        // 逐个server发送
        for (Map.Entry<Integer, List<IMUserInfo>> entry : serverMap.entrySet()) {
            IMRecvInfo recvInfo = new IMRecvInfo();
            recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
            recvInfo.setReceivers(new LinkedList<>(entry.getValue()));
            recvInfo.setSender(message.getSender());
            recvInfo.setServiceName(appName);
            recvInfo.setSendResult(message.getSendResult());
            recvInfo.setData(message.getData());
            // 推送至队列
            String key = String.join(":", IMRedisKey.IM_MESSAGE_GROUP_QUEUE, entry.getKey().toString());
            redisMQTemplate.opsForList().rightPush(key, recvInfo);
        }

        // 推送给自己的其他终端
        if (message.getSendToSelf()) {  // todo 做一个判断
            for (Integer terminal : IMTerminalType.codes()) {    // todo 遍历terminal
                if (terminal.equals(message.getSender().getTerminal())) {
                    continue;
                }
                // 获取终端连接的channelId
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, message.getSender().getId().toString(), terminal.toString());
                Integer serverId = (Integer) redisMQTemplate.opsForValue().get(key);
                // 如果终端在线，将数据存储至redis，等待拉取推送
                if (!Objects.isNull(serverId)) {
                    IMRecvInfo recvInfo = new IMRecvInfo();
                    recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    recvInfo.setSender(message.getSender());
                    recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getId(), terminal)));
                    // 自己的消息不需要回推消息结果
                    recvInfo.setSendResult(false);
                    recvInfo.setData(message.getData());
                    String sendKey = String.join(":", IMRedisKey.IM_MESSAGE_GROUP_QUEUE, serverId.toString());
                    redisMQTemplate.opsForList().rightPush(sendKey, recvInfo);
                }
            }
        }
        // 对离线用户回复消息状态
        if (message.getSendResult() && !offLineUsers.isEmpty()) {
            List<IMSendResult> results = new LinkedList<>();
            for (IMUserInfo offLineUser : offLineUsers) {
                IMSendResult result = new IMSendResult();
                result.setSender(message.getSender());
                result.setReceiver(offLineUser);
                result.setCode(IMSendCode.NOT_ONLINE.code());
                result.setData(message.getData());
                results.add(result);
            }
            listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, results);
        }

    }

    /**
     * 获取在线的终端
     *
     * @param userIds
     * @return
     */
    public Map<Long, List<IMTerminalType>> getOnlineTerminal(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        //todo 把所有用户的任何终端，用key都存起来
        Map<String, IMUserInfo> userMap = new HashMap<>();
        for (Long id : userIds) {
            for (Integer terminal : IMTerminalType.codes()) {
                String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, id.toString(), terminal.toString());
                userMap.put(key, new IMUserInfo(id, terminal));
            }
        }
        // 批量拉取
        List<Object> serverIds = redisMQTemplate.opsForValue().multiGet(userMap.keySet());
        int idx = 0;
        Map<Long, List<IMTerminalType>> onlineMap = new HashMap<>();
        for (Map.Entry<String, IMUserInfo> entry : userMap.entrySet()) {
            // serverid有值表示用户在线
            if (serverIds.get(idx++) != null) {
                IMUserInfo userInfo = entry.getValue();
                // 如果这个用户ID在映射中还没有对应的列表，那么就创建一个新的LinkedList实例，并将其作为这个用户ID的值插入到映射中
                List<IMTerminalType> terminals = onlineMap.computeIfAbsent(userInfo.getId(), o -> new LinkedList<>());
                terminals.add(IMTerminalType.fromCode(userInfo.getTerminal()));
            }
        }
        // 去重并返回
        return onlineMap;
    }

    public Boolean isOnline(Long userId) {
        String key = String.join(":", IMRedisKey.IM_USER_SERVER_ID, userId.toString(), "*");
        return !Objects.requireNonNull(redisMQTemplate.keys(key)).isEmpty();
    }

    public List<Long> getOnlineUser(List<Long> userIds) {
        return new LinkedList<>(getOnlineTerminal(userIds).keySet());
    }
}
