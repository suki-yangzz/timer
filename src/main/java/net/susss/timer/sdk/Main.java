package net.susss.timer.sdk;

import lombok.Data;
import lombok.ToString;
import net.susss.timer.api.TiRedissonClient;
import net.susss.timer.api.Timer;
import net.susss.timer.common.Constants;
import net.susss.timer.redisson.TiRedissonBlockingQueue;
import net.susss.timer.redisson.TiRedissonDelayedQueue;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScoredSortedSet;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Data
@ToString
public class Main implements Timer {

    TiRedissonClient redisson;

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
            TiRedissonBlockingQueue<String> blockingQueue = redisson.getTiBlockingQueue(Constants.DELAY_QUEUE);
            TiRedissonDelayedQueue<String> delayedQueue = redisson.getTiDelayedQueue(blockingQueue);
            delayedQueue.offer(bucket + Constants.ESCAPE_BUCKET + startTime + Constants.ESCAPE_STARTTIME + key, timeout, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unset(String key, long startTime, TimeUnit timeUnit, long timeout) {
        try {
            String bucket = String.valueOf(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
            String setValue = startTime + Constants.ESCAPE_STARTTIME + key;
            RScoredSortedSet set = redisson.getScoredSortedSet(bucket);
            set.remove(setValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
