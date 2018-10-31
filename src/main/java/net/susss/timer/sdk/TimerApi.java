package net.susss.timer.sdk;

import com.sun.tools.javac.util.ArrayUtils;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.xml.ws.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Suki Yang on 10/24/2018.
 */
@Data
public class TimerApi implements Timer {

    /**
     * logger of slf4j
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TimerApi.class);

    private JedisPool jedisPool;

    private JedisPoolConfig poolConfig;

    private String             host;

    private int                port        = 6379;

    private String             password;

    private int                timeout     = 20000;

    private ThreadLocal<Jedis> JEDIS_LOCAL = new ThreadLocal<Jedis>();

    private final String       ENCODING    = "UTF-8";

    private int database = 0;

    private static final int PIPE_LIMIT = 5000;

    @Override
    public void init() {
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
    }


    private Jedis get() {
        final Jedis jedis = jedisPool.getResource();
        JEDIS_LOCAL.set(jedis);
        return jedis;
    }

    private String getKey(CacheEnum cache, String suffixKey) {
        return cache.getCode() + suffixKey;
    }

    private void close() {
        if (null != JEDIS_LOCAL.get()) {
            JEDIS_LOCAL.get().close();
        }
    }

    @Override
    public void putWithExpire(CacheEnum cache, Serializable value, int expire) {
        putWithExpire(cache.getCode(), value, expire);
    }

    private void putWithExpire(String key, Serializable value, int expire) {


        try {
            final Jedis jedis = get();
            if (isSimpleObject(value)) {
                jedis.set(key, String.valueOf(value));
            } else {
                jedis.set(key.getBytes(ENCODING), SerializationUtils.serialize(value));
            }
            if (expire > 0) {
                jedis.expire(key, expire);
            }
        } catch (Exception e) {

        } finally {
            close();
        }
    }

    @Override
    public <T> T get(CacheEnum cache) {
        return get(cache.getCode());
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String key) {

        try {
            final Jedis jedis = get();

            final byte[] datas = jedis.get(key.getBytes(ENCODING));
            if (null==datas) {
                return null;
            }
            return (T) SerializationUtils.deserialize(datas);
        } catch (SerializationException se) {
            return (T) getString(key);
        } catch (Exception e) {

        } finally {
            close();
        }
        return null;
    }

    private boolean isSimpleObject(Serializable value) {
        final Class<?> clazz = value.getClass();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz);
    }

    private String getString(String key) {
        try {
            return get().get(key);
        } catch (Exception e) {

        }
        return key;
    }

    @Override
    public <T> T get(CacheEnum cache, String suffixKey) {
        return get(getKey(cache, suffixKey));
    }

    @Override
    public void remove(CacheEnum cache) {
        remove(cache.getCode());
    }

    private void remove(String key) {
        try {
            final Jedis jedis = get();
            jedis.del(key);
        } catch (Exception e) {

        } finally {
            close();
        }
    }

    @Override
    public void remove(CacheEnum cache, String suffixKey) {
        remove(getKey(cache, suffixKey));
    }

    /**
     * Key-Value Pair
     *
     * @author RobertJ
     */
    public static class Pair{
        private String key;
        private String value;

        public Pair(String key, String value){
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
