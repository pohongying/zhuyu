package com.luoyi.imserver.netty;

import com.luoyi.imcommon.contant.IMRedisKey;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动和关闭时能正确初始化和停止一组IMServer实例的组件。
 */
@Slf4j
@Component
@AllArgsConstructor
public class IMServerGroup implements CommandLineRunner {

    /**
     * volatile: 保证可见性  防止指令重排序
     */
    public static volatile long serverId = 0;

    private final  RedisTemplate<String, Object> redisTemplate;

    private final List<IMServer> imServers;

    /***
     * 判断服务器是否就绪
     *
     * @return
     **/
    public boolean isReady() {
        for (IMServer imServer : imServers) {
            if (!imServer.isReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run(String... args) {  // todo run 方法会在 ApplicationRunner 的 run 方法之后执行
        // 初始化SERVER_ID
        String key = IMRedisKey.IM_MAX_SERVER_ID;
        serverId = redisTemplate.opsForValue().increment(key, 1); // TODO: 2024/8/27 自增的serveId 
        // 启动服务
        for (IMServer imServer : imServers) {
            imServer.start();
        }
    }

    @PreDestroy
    public void destroy() {
        // 停止服务
        for (IMServer imServer : imServers) {
            imServer.stop();
        }
    }
}
