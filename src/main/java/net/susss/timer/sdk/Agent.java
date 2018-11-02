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

    private Executor executor;

    private DelayedQueueConsumer consumerBean;

    private String address;

    private String password;

    private int database = 0;

    private int timeout = 20000;

    private String mode;

    private int poolSize;

    private int poolMinIdleSize;

    public Agent() throws Exception {
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
//        ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:/spring/applicationContext-agent.xml");
//        TaskExecutor taskExecutor = (TaskExecutor) appContext.getBean("taskExecutor");
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
//        this.executor = taskExecutor;
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
        LOGGER.info("start..........");
        if (executor instanceof ExecutorConfigurationSupport) {
            ((ExecutorConfigurationSupport) executor).initialize();
        }

        Properties properties = new Properties();
        properties.setProperty(Constants.REDIS_ADDRESS, address);
        properties.setProperty(Constants.REDIS_PASSWORD, password);
        properties.setProperty(Constants.REDIS_DATABASE, String.valueOf(database));
        properties.setProperty(Constants.REDIS_TIMEOUT, String.valueOf(timeout));
        properties.setProperty(Constants.REDIS_MODE, mode);
        properties.setProperty(Constants.REDIS_CLIENT_POOL_SIZE, String.valueOf(poolSize));
        properties.setProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE, String.valueOf(poolMinIdleSize));

        for (int i = 0; i < Constants.MAX_AGENT_SIZE; i++) {
            DelayedQueueConsumer t = new DelayedQueueConsumer(properties);
            executor.execute(t);
        }


    }

//    private void startAgentBean() {
//        LOGGER.info("start agent bean..........");
//        if (consumerBean == null) {
//            Properties properties = new Properties();
//            properties.setProperty(Constants.REDIS_ADDRESS, address);
//            properties.setProperty(Constants.REDIS_PASSWORD, password);
//            properties.setProperty(Constants.REDIS_DATABASE, String.valueOf(database));
//            properties.setProperty(Constants.REDIS_TIMEOUT, String.valueOf(timeout));
//            properties.setProperty(Constants.REDIS_MODE, mode);
//            properties.setProperty(Constants.REDIS_CLIENT_POOL_SIZE, String.valueOf(poolSize));
//            properties.setProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE, String.valueOf(poolMinIdleSize));
//            consumerBean = new DelayedQueueConsumer(properties);
//        }
//    }

    /***
     * Destroy method
     */
    @PreDestroy
    public void shutdown() {
        LOGGER.info("shutdown..........");
        consumerBean.shutdown();
        if (executor instanceof ExecutorConfigurationSupport) {
            ((ExecutorConfigurationSupport) executor).shutdown();
        }
    }
}
