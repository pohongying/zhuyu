package com.luoyi.imcommon.mq;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Properties;

/**
 * 相当于自己又重新封装了一些方法
 */
public class RedisMQTemplate extends RedisTemplate<String, Object> {

    private String version = Strings.EMPTY;

    public String getVersion() {
        if (version.isEmpty()) {
            // 开启一段连接
            RedisConnection redisConnection = this.getConnectionFactory().getConnection();
            Properties properties = redisConnection.info();
            version = properties.getProperty("redis_version");
        }
        return version;
    }

    /**
     * 是否支持批量拉取，redis版本大于6.2支持批量拉取
     *
     * @return
     */
    Boolean isSupportBatchPull() {
        String version = getVersion();
        String[] arr = version.split("\\.");
        if (arr.length < 2) {
            return false;
        }
        Integer firVersion = Integer.valueOf(arr[0]);
        Integer secVersion = Integer.valueOf(arr[1]);
        return firVersion > 6 || (firVersion == 6 && secVersion >= 2);
    }


    /**
     * 连接是否关闭
     * @return
     */
    Boolean isClose() {
        try {
            return getConnectionFactory().getConnection().isClosed();
        } catch (Exception e) {
            return true;
        }
    }
}
