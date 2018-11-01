package net.susss.timer.redis;

import net.susss.timer.sdk.Constants;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 10/24/2018.
 */
public class RedisProducer {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConsumer.class);

    private Properties properties;

    public RedisProducer(Properties properties) {
        this.properties = properties;
    }

    public void set(String key, String value, long startTime, TimeUnit timeUnit, long timeout) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + properties.getProperty(Constants.REDIS_ADDR))
                .setConnectionPoolSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)))
                .setConnectionMinimumIdleSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)));
        RedissonClient client = Redisson.create(config);
        RDelayedQueue<String> delayedQueue = null;
        try {
            String bucket = String.valueOf(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
            String setVal = startTime + Constants.ESCAPE_BUCKET + key;
            RScoredSortedSet set = client.getScoredSortedSet(bucket);
            RBlockingQueue<String> blockingQueue = client.getBlockingQueue("delay_queue");
            delayedQueue = client.getDelayedQueue(blockingQueue);
            boolean add = set.add(startTime, setVal);
            delayedQueue.offer(startTime + Constants.ESCAPE_STARTTIME + key, timeout, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != delayedQueue) delayedQueue.destroy();
            client.shutdown();
        }
    }

    public void unset(String key, long startTime, TimeUnit timeUnit, long timeout) {

    }
}
