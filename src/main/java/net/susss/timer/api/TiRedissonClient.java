package net.susss.timer.api;

import net.susss.timer.redisson.TiRedissonBlockingQueue;
import net.susss.timer.redisson.TiRedissonDelayedQueue;
import org.redisson.api.RedissonClient;

/**
 * Created by Suki Yang on 11/2/2018.
 */
public interface TiRedissonClient extends RedissonClient {
    TiRedissonBlockingQueue<String> getTiBlockingQueue(String delayQueue);

    TiRedissonDelayedQueue<String> getTiDelayedQueue(TiRedissonBlockingQueue<String> blockingQueue);
}
