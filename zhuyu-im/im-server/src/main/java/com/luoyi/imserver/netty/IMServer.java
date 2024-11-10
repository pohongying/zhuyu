package com.luoyi.imserver.netty;


/**
 * IM服务接口
 * 负责IM服务启动、关闭、状态查询
 */
public interface IMServer {

    boolean isReady();

    void start();

    void stop();
}
