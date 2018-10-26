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
import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 10/24/2018.
 */
public class RedisProducer implements Runnable {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConsumer.class);

    private Thread t;
    private Properties properties;

    public RedisProducer(Properties properties) {
        this.properties = properties;
    }

    public void run() {
        LOGGER.info("Running");
        Config config = new Config();
        config.useSingleServer()
                .setAddress(properties.getProperty(Constants.REDIS_ADDR))
                .setConnectionPoolSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)))
                .setConnectionMinimumIdleSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)));
        RedissonClient client = Redisson.create(config);
        RBlockingQueue<String> blockingQueue = client.getBlockingQueue("delay_queue");
        RDelayedQueue<String> delayedQueue = client.getDelayedQueue(blockingQueue);
        while(true) {
            try {
                String key = String.valueOf(new Date().getTime());
                key = key + key + key;

                //首先获取redis中的key-value对象，key不存在没关系
                RBucket<String> keyObject = client.getBucket(key);
                //如果key存在，就设置key的值为新值value
                //如果key不存在，就设置key的值为value
                keyObject.set("Processor_Flag");
                delayedQueue.offer(key, 5 * 1000, TimeUnit.MILLISECONDS);
                LOGGER.info("Thread Offer " + key + " at Time: "
                        + new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                delayedQueue.destroy();
                client.shutdown();
            }
            LOGGER.info("Thread exiting.");
        }
    }

    public void start() {
        LOGGER.info("Starting");
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
