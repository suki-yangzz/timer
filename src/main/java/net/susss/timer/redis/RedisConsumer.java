package net.susss.timer.redis;

import net.susss.timer.sdk.Constants;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Suki Yang on 10/23/2018.
 */
public class RedisConsumer implements Runnable {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConsumer.class);

    private Thread t;
    private Properties properties;

    public RedisConsumer(Properties properties) {
        this.properties = properties;
    }

    public void run() {
        LOGGER.info("Running ");
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + properties.getProperty(Constants.REDIS_ADDR))
                .setConnectionPoolSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)))
                .setConnectionMinimumIdleSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)));
        RedissonClient client = Redisson.create(config);
        RBlockingQueue<String> blockingQueue = client.getBlockingQueue("delay_queue");
        RDelayedQueue<String> delayedQueue = client.getDelayedQueue(blockingQueue);

        while (true) {
            try {
                String key = blockingQueue.take();
                RBucket<String> keyObject = client.getBucket(key);
                if (keyObject.isExists()) {
                    //Identify as timeout
                    //TODO any non-blocking callback here
                    keyObject.getAndDelete();
                }
                LOGGER.info("Consumer Takes " + key + "(" + keyObject.get() + ")"
                        + " at Time: " + new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        LOGGER.info("Starting...");
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
