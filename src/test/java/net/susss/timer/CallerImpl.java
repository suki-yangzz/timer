package net.susss.timer;

import net.susss.timer.sdk.Constants;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 10/24/2018.
 */
@Service("Caller")
public class CallerImpl {

    public void set(String key, String value, long startTime, TimeUnit timeUnit, long timeout) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setConnectionPoolSize(10).setConnectionMinimumIdleSize(5);
        RedissonClient client = Redisson.create(config);
        RBlockingQueue<String> blockingQueue = client.getBlockingQueue("delay_queue");
        RDelayedQueue<String> delayedQueue = client.getDelayedQueue(blockingQueue);
        try {
            String bucket = String.valueOf(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
            String setVal = startTime + Constants.ESCAPE_BUCKET + key;
            RScoredSortedSet set = client.getScoredSortedSet(bucket);
            boolean add = set.add(startTime, setVal);
            delayedQueue.offer(startTime + Constants.ESCAPE_STARTTIME + key, timeout, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            delayedQueue.destroy();
            client.shutdown();
        }
    }

    public void unset(String key, long startTime, TimeUnit timeUnit, long timeout) {

    }
}
