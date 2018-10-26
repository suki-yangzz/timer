package net.susss.timer.sdk;

import net.susss.timer.redis.RedisConsumer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Created by Suki Yang on 10/23/2018.
 */
@Getter @Setter
public class Reporter {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Reporter.class);

    /**
     * Redis address, default is 127.0.0.1:6379
     */
    private String redisAddr;

    private String clientPoolSize;

    private String clientIdleSize;

    private Executor executor;

    public Reporter() throws Exception {
        Properties properties = new Properties();
        InputStreamReader reader =null;
        try {
            reader = new InputStreamReader(this.getClass().getResourceAsStream("/timer.properties"), "UTF-8");
            properties.load(reader);

        } finally {
            if(reader!= null) {
                close(reader);
            }
        }
        ThreadPoolTaskExecutor defaultExecutor = new ThreadPoolTaskExecutor();
        defaultExecutor.setThreadGroupName(properties.getProperty("callerExecutor.threadGroupName"));
        defaultExecutor.setThreadNamePrefix(properties.getProperty("callerExecutor.threadNamePrefix"));
        defaultExecutor.setCorePoolSize(Integer.valueOf(properties.getProperty("callerExecutor.corePoolSize")));
        defaultExecutor.setMaxPoolSize(Integer.valueOf(properties.getProperty("callerExecutor.maxPoolSize")));
        defaultExecutor.setKeepAliveSeconds(Integer.valueOf(properties.getProperty("callerExecutor.keepAliveSeconds")));
        defaultExecutor.setQueueCapacity(Integer.valueOf(properties.getProperty("callerExecutor.queueCapacity")));
        defaultExecutor.setWaitForTasksToCompleteOnShutdown(true);
        defaultExecutor.setAllowCoreThreadTimeOut(true);
        this.executor = defaultExecutor;
    }

    private void close(InputStreamReader reader) {
        if(reader!= null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOGGER.error("close input stream reader error", e);
            }
        }
    }

    /**
     * init method.
     */
    @PostConstruct
    public void start() {
        LOGGER.info("start caller............!");
        Assert.notNull(redisAddr, "property redisAddr cannot null");
        if (executor instanceof ExecutorConfigurationSupport) {
            ((ExecutorConfigurationSupport) executor).initialize();
        }

        initReporterPool();
    }

    private void initReporterPool() {
        LOGGER.info("init Redis Producer..........");
        Properties properties = new Properties();
        properties.setProperty(Constants.REDIS_ADDR, redisAddr);
        properties.setProperty(Constants.REDIS_CLIENT_POOL_SIZE, clientPoolSize);
        properties.setProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE, clientIdleSize);
        for (int i = 1; i <= Integer.parseInt(clientPoolSize); i++) {
            RedisConsumer r = new RedisConsumer(properties);
            r.start();
        }
    }
}
