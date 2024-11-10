package com.luoyi.implatform.fastdfs.config;

import jakarta.annotation.PostConstruct;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FastDFSConfig {

    @Value("${fastdfs.tracker_servers}")
    private String trackerList;

    @PostConstruct
    public void init() throws IOException, MyException {
        ClientGlobal.initByTrackers(trackerList);
    }

    public TrackerClient trackerClient() {
        return new TrackerClient();
    }

    @Bean
    public TrackerServer getTrackerServer() throws IOException {
        return trackerClient().getConnection();
    }
}
