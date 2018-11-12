package net.susss.timer.sdk;

import lombok.Data;
import net.susss.timer.common.Constants;
import net.susss.timer.internal.DelayedQueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Created by Suki Yang on 10/23/2018.
 */
@Data
public class Agent {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    private DelayedQueueConsumer t;

    private String address;

    private String master;

    private String slave;

    private String password;

    private int database = 0;

    private int timeout = 20000;

    private String mode;

    private int poolSize;

    private int poolMinIdleSize;

    public Agent() {}

    /**
     * init method.
     */
    @PostConstruct
    public void start() {
        LOGGER.info("TimerApi Agent starting.");

        Properties properties = new Properties();
        properties.setProperty(Constants.REDIS_PASSWORD, password);
        properties.setProperty(Constants.REDIS_DATABASE, String.valueOf(database));
        properties.setProperty(Constants.REDIS_TIMEOUT, String.valueOf(timeout));
        properties.setProperty(Constants.REDIS_MODE, mode);
        switch (mode) {
            case Constants.REDIS_MODE_SINGLE:
                properties.setProperty(Constants.REDIS_ADDRESS, address);
                properties.setProperty(Constants.REDIS_CLIENT_POOL_SIZE, String.valueOf(poolSize));
                properties.setProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE, String.valueOf(poolMinIdleSize));
                break;
            case Constants.REDIS_MODE_MASTER_SLAVE:
                properties.setProperty(Constants.REDIS_MASTER, master);
                properties.setProperty(Constants.REDIS_SLAVE, slave);
                break;
            default:
                properties.setProperty(Constants.REDIS_MODE, Constants.REDIS_MODE_SINGLE);
                properties.setProperty(Constants.REDIS_ADDRESS, Constants.DEFAULT_REDIS_HOST + ":" + Constants.DEFAULT_REDIS_PORT);
                properties.setProperty(Constants.REDIS_CLIENT_POOL_SIZE, String.valueOf(10));
                properties.setProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE, String.valueOf(5));
                break;
        }

        for (int i = 0; i < Constants.MAX_AGENT_SIZE; i++) {
            taskExecutor.execute(new DelayedQueueConsumer(properties));
        }


    }

    /***
     * Destroy method
     */
    @PreDestroy
    public void shutdown() {
        LOGGER.info("TimerApi Agent shutting down.");
        taskExecutor.shutdown();
    }
}
