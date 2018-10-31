package net.susss.timer.sdk;

import net.susss.timer.redis.RedisConsumer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;

public class TimerMainApi implements Timer {

    private Properties properties;

    @Override
    public void init() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + properties.getProperty(Constants.REDIS_ADDR))
                .setConnectionPoolSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_POOL_SIZE)))
                .setConnectionMinimumIdleSize(Integer.parseInt(properties.getProperty(Constants.REDIS_CLIENT_MIN_IDLE_SIZE)));
        RedissonClient client = Redisson.create(config);
    }

    @Override
    public void putWithExpire(CacheEnum cache, Serializable value, int expire) {

    }

    @Override
    public <T> T get(CacheEnum cache) {
        return null;
    }

    @Override
    public <T> T get(CacheEnum cache, String suffixKey) {
        return null;
    }

    @Override
    public void remove(CacheEnum cache) {

    }

    @Override
    public void remove(CacheEnum cache, String suffixKey) {

    }

}
