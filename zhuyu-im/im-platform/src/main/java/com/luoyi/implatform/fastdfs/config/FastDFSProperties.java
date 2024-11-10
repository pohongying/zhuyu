package com.luoyi.implatform.fastdfs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fastdfs")
public class FastDFSProperties {

    private int connectTimeoutInSeconds;    // 连接超时时间
    private int networkTimeoutInSeconds;    // 网络超时
    private String charset;                 // 编码
    private boolean httpAntiStealToken;     // 是否开启防盗链
    private String httpSecretKey;           // 防盗链密钥
    private int httpTrackerHttpPort;        // tracker的http端口
    private String trackerServers;          // tracker的IP地址
    private int maxStorageConnection;       // 最大连接数
    private String fileServerAddr;        // 访问storage文件服务器方法

    public FastDFSProperties() {
    }

    public FastDFSProperties(int connectTimeoutInSeconds, int networkTimeoutInSeconds, String charset, boolean httpAntiStealToken, String httpSecretKey, int httpTrackerHttpPort, String trackerServers, int maxStorageConnection, String fileServerAddr) {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
        this.networkTimeoutInSeconds = networkTimeoutInSeconds;
        this.charset = charset;
        this.httpAntiStealToken = httpAntiStealToken;
        this.httpSecretKey = httpSecretKey;
        this.httpTrackerHttpPort = httpTrackerHttpPort;
        this.trackerServers = trackerServers;
        this.maxStorageConnection = maxStorageConnection;
        this.fileServerAddr = fileServerAddr;
    }

    // Getters and Setters

    public int getConnectTimeoutInSeconds() {
        return connectTimeoutInSeconds;
    }

    public void setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
    }

    public int getNetworkTimeoutInSeconds() {
        return networkTimeoutInSeconds;
    }

    public void setNetworkTimeoutInSeconds(int networkTimeoutInSeconds) {
        this.networkTimeoutInSeconds = networkTimeoutInSeconds;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isHttpAntiStealToken() {
        return httpAntiStealToken;
    }

    public void setHttpAntiStealToken(boolean httpAntiStealToken) {
        this.httpAntiStealToken = httpAntiStealToken;
    }

    public String getHttpSecretKey() {
        return httpSecretKey;
    }

    public void setHttpSecretKey(String httpSecretKey) {
        this.httpSecretKey = httpSecretKey;
    }

    public int getHttpTrackerHttpPort() {
        return httpTrackerHttpPort;
    }

    public void setHttpTrackerHttpPort(int httpTrackerHttpPort) {
        this.httpTrackerHttpPort = httpTrackerHttpPort;
    }

    public String getTrackerServers() {
        return trackerServers;
    }

    public void setTrackerServers(String trackerServers) {
        this.trackerServers = trackerServers;
    }

    public int getMaxStorageConnection() {
        return maxStorageConnection;
    }

    public void setMaxStorageConnection(int maxStorageConnection) {
        this.maxStorageConnection = maxStorageConnection;
    }

    /**
     * 获取
     * @return fileServerAddr
     */
    public String getFileServerAddr() {
        return fileServerAddr;
    }

    /**
     * 设置
     * @param fileServerAddr
     */
    public void setFileServerAddr(String fileServerAddr) {
        this.fileServerAddr = fileServerAddr;
    }

    public String toString() {
        return "FastDFSProperties{connectTimeoutInSeconds = " + connectTimeoutInSeconds + ", networkTimeoutInSeconds = " + networkTimeoutInSeconds + ", charset = " + charset + ", httpAntiStealToken = " + httpAntiStealToken + ", httpSecretKey = " + httpSecretKey + ", httpTrackerHttpPort = " + httpTrackerHttpPort + ", trackerServers = " + trackerServers + ", maxStorageConnection = " + maxStorageConnection + ", fileServerAddr = " + fileServerAddr + "}";
    }
}
