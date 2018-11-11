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

    private Thread t;

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

        String mode = properties.getProperty(Constants.REDIS_MODE);
        if (StringUtils.isBlank(mode)) {
            mode = Constants.REDIS_MODE_SINGLE;
        }
        switch (mode) {
            case Constants.REDIS_MODE_SINGLE:
                redisson = new RedisClient()
                        .redissonSingle(properties.getProperty(Constants.REDIS_ADDRESS),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_TIMEOUT)),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)),
                                Integer.valueOf(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)),
                                properties.getProperty(Constants.REDIS_PASSWORD));
        }

        TiRedissonBlockingQueue<String> blockingQueue = redisson.getTiBlockingQueue(Constants.DELAY_QUEUE);
        RSortedSet<String> timeoutSet = redisson.getSortedSet(Constants.TIMEOUT_SET);

        while (true) {
            try {
                //key: bucket%startTime_uniqueID
                String key = blockingQueue.checkTimeout();
                System.out.println("Processed Key: " + key);
                if (null != key) {
                    RScoredSortedSet set = redisson.getScoredSortedSet(key.substring(0, key.indexOf(Constants.ESCAPE_BUCKET)));
                    String element = key.substring(key.indexOf(Constants.ESCAPE_BUCKET) + 1, key.length());
                    if (!set.contains(element)) {
                        timeoutSet.remove(key);
                    }
                }
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
//            } catch (NullPointerException ex2) {
//                System.out.println("key null");
//                ex2.printStackTrace();
//                continue;
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
