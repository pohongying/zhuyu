package com.luoyi.imclient.multicast;

import cn.hutool.core.util.StrUtil;
import com.luoyi.imcommon.mq.RedisMQConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public abstract class AbstractMessageMultiCastTask<T> extends RedisMQConsumer<T> {

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public String generateKey() {
        return StrUtil.join(":", super.generateKey(), appName);
    }



}
