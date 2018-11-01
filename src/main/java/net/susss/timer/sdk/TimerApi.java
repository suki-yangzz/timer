package net.susss.timer.sdk;

import lombok.Data;
import lombok.ToString;
import net.susss.timer.redis.RedisClient;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Data
@ToString
public class TimerApi implements Timer {

    RedissonClient redisson;

    private String address;

    private String password;

    private int database = 0;

    private int timeout = 20000;

    private String mode;

    private int poolSize;

    private int poolMinIdleSize;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(mode)) {
            mode = Constants.REDIS_MODE_SINGLE;
        }
        switch (mode) {
            case Constants.REDIS_MODE_SINGLE:
                redisson = new RedisClient().redissonSingle(address, timeout, poolSize, poolMinIdleSize, password);
        }
    }

    @Override
    public void set(String key, String value, long startTime, TimeUnit timeUnit, long timeout) {
        try {
            String bucket = String.valueOf(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
            String setValue = startTime + Constants.ESCAPE_STARTTIME + key;
            RScoredSortedSet set = redisson.getScoredSortedSet(bucket);
            set.add(startTime, setValue);
            RDelayedQueue<String> delayedQueue = redisson.getDelayedQueue(redisson.getBlockingQueue(Constants.DELAY_QUEUE));
            delayedQueue.offer(timeout + Constants.ESCAPE_BUCKET + startTime + Constants.ESCAPE_STARTTIME + key, timeout, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        }
//            TODO: close or not
//        } finally {
//            if (null != delayedQueue) delayedQueue.destroy();
//            redisson.shutdown();
//        }
    }

    @Override
    public void unset(String key, long startTime, TimeUnit timeUnit, long timeout) {
        try {
            String bucket = String.valueOf(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
            String setValue = startTime + Constants.ESCAPE_BUCKET + key;
            RScoredSortedSet set = redisson.getScoredSortedSet(bucket);
            RDelayedQueue<String> delayedQueue = redisson.getDelayedQueue(redisson.getBlockingQueue(Constants.DELAY_QUEUE));
            set.remove(setValue);
            delayedQueue.offer(startTime + Constants.ESCAPE_STARTTIME + key, timeout, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
