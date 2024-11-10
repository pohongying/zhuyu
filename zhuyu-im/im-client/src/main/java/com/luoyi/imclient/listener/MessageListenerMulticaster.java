package com.luoyi.imclient.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.luoyi.imcommon.annotation.IMListener;
import com.luoyi.imcommon.enums.IMListenerType;
import com.luoyi.imcommon.model.IMSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * 消息监听器多播器，用于向多个消息监听器广播消息
 */
@Component
public class MessageListenerMulticaster {

    @Autowired(required = false)  // 非必要的
    private List<MessageListener> messageListeners = Collections.emptyList();

    /**
     * 向所有注册的消息监听器广播消息
     *
     * @param listenerType 监听器类型，用于筛选需要接收消息的监听器
     * @param results      消息发送结果列表，包含每次消息发送的详细结果
     */
    public void multicast(IMListenerType listenerType, List<IMSendResult> results) {
        // 如果结果列表为空，则直接返回
        if (CollUtil.isEmpty(results)) {
            return;
        }
        for (MessageListener listener : messageListeners) {
            // 获取监听器上的IMListener注解
            IMListener annotation = listener.getClass().getAnnotation(IMListener.class);
            // 如果注解存在且监听器类型为全部或指定类型，则处理消息
            if (annotation != null && (annotation.type().equals(IMListenerType.ALL) || annotation.type().equals(listenerType))) {
                results.forEach(result -> {
                    // 将data转回对象类型
                    if (result.getData() instanceof JSONObject) {
                        Type superClass = listener.getClass().getGenericInterfaces()[0];
                        Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
                        JSONObject data = (JSONObject) result.getData();
                        // 将JSON对象转换回其原始对象类型
                        result.setData(data.toJavaObject(type));
                    }
                });
                // 回调到调用方处理
                listener.process(results);   // todo 调用各个消息监听器的process方法
            }
        }
    }


}
