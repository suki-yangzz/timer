package net.susss.timer.internal;

import lombok.Data;
import net.susss.timer.api.TiRedissonClient;
import net.susss.timer.common.Constants;
import net.susss.timer.redisson.TiRedissonBlockingQueue;
import net.susss.timer.sdk.RedisClient;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.SortedSet;

/**
 * Created by Suki Yang on 10/23/2018.
 */
@Data
public class DelayedQueueConsumer extends Thread {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedQueueConsumer.class);

    private Properties properties;

    TiRedissonClient redisson;


    public DelayedQueueConsumer(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        if (null == this.properties) {
            LOGGER.error("empty properties");
        } else {
            LOGGER.info("Running ");
        }

        switch (properties.getProperty(Constants.REDIS_MODE)) {
            case Constants.REDIS_MODE_SINGLE:
                redisson = new RedisClient()
                        .redissonSingle(properties.getProperty(Constants.REDIS_ADDRESS),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_TIMEOUT)),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)),
                                properties.getProperty(Constants.REDIS_PASSWORD));
                break;
            case Constants.REDIS_MODE_MASTER_SLAVE:
                redisson = new RedisClient()
                        .redissonMasterSlave(properties.getProperty(Constants.REDIS_MASTER),
                                properties.getProperty(Constants.REDIS_SLAVE),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_TIMEOUT)),
                                properties.getProperty(Constants.REDIS_PASSWORD));
                break;
            default:
                redisson = new RedisClient()
                        .redissonSingle(properties.getProperty(Constants.REDIS_ADDRESS),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_TIMEOUT)),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)),
                                properties.getProperty(Constants.REDIS_PASSWORD));
                break;
        }

        TiRedissonBlockingQueue<String> blockingQueue = redisson.getTiBlockingQueue(Constants.DELAY_QUEUE);

        while (true) {
            try {
                //key: bucket%startTime_uniqueID
                String key = blockingQueue.checkTimeout();
                if (null != key) {
                    LOGGER.info("Agent identified a timeout Key as " + key);
                }
            } catch (InterruptedException ex1) {
                LOGGER.error("Agent caught error as", ex1.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (this.redisson != null) {
            this.redisson.shutdown();
        }
    }
}
