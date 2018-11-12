package net.susss.timer.sdk;

import lombok.Data;
import lombok.ToString;
import net.susss.timer.api.TiRedissonClient;
import net.susss.timer.api.TimerApi;
import net.susss.timer.common.Constants;
import net.susss.timer.redisson.TiRedissonBlockingQueue;
import net.susss.timer.redisson.TiRedissonDelayedQueue;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScoredSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Data
@ToString
public class Timer implements TimerApi {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

    TiRedissonClient redisson;

    private String address;

    private String master;

    private String slave;

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
        if (StringUtils.isBlank(address)) {
            address = Constants.DEFAULT_REDIS_HOST + ":" + Constants.DEFAULT_REDIS_PORT;
        }
        switch (mode) {
            case Constants.REDIS_MODE_SINGLE:
                redisson = new RedisClient().redissonSingle(address, timeout, poolSize, poolMinIdleSize, password);
                break;
            case Constants.REDIS_MODE_MASTER_SLAVE:
                redisson = new RedisClient().redissonMasterSlave(master, slave, timeout, password);
                break;
            default:
                redisson = new RedisClient().redissonSingle(address, timeout, poolSize, poolMinIdleSize, password);
                break;
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
            LOGGER.error("TimerApi Main set method error: ", e.getMessage());
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
            LOGGER.error("TimerApi Main unset method error: ", e.getMessage());
        }
    }

    @Override
    public String handle() {
        try {
            RScoredSortedSet<String> timeout_set = redisson.getScoredSortedSet(Constants.TIMEOUT_SET);
            return timeout_set.pollFirst();
        } catch (Exception e) {
            LOGGER.error("TimerApi Main handle method error: ", e.getMessage());
        }
        return null;
    }
}
