package com.luoyi.implatform.task;

import com.luoyi.imcommon.mq.RedisMQConsumer;

/**
 * @Description Speak less ,type more code
 * @Author Luoyi
 * @Date 2024/9/19
 */
public abstract class AbstractBanTask<T> extends RedisMQConsumer<T> {

}