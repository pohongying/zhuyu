package com.luoyi.imserver.task;

import com.luoyi.imcommon.mq.RedisMQConsumer;
import com.luoyi.imserver.netty.IMServerGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 *推送消息任务，正式调用推送信息
 */
@Slf4j
public abstract class AbstractPullMessageTask<T> extends RedisMQConsumer<T> {

    @Autowired
    private IMServerGroup serverGroup;

    @Override
    public String generateKey() {
        return String.join(":",  super.generateKey(), IMServerGroup.serverId + "");
    }

    @Override
    public Boolean isReady() {
        return serverGroup.isReady();
    }
}
