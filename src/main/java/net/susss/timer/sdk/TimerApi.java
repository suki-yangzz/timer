package net.susss.timer.sdk;

import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;

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

    private JedisPool          jedisPool;

    private JedisPoolConfig    poolConfig;

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
        AssertUtil.notBlank(suffixKey, "suffix key is empty");
        return cache.getCode() + suffixKey;
    }

    private void close() {
        if (null != JEDIS_LOCAL.get()) {
            JEDIS_LOCAL.get().close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#increment(com.eigpay.common.cache.enums.CacheEnum)
     */
    @Override
    public Long increment(CacheEnum cache) throws CustomerException {
        return increment(cache.getCode());
    }

    /**
     * @see com.eigpay.common.cache.CacheService#increment(com.eigpay.common.cache.enums.CacheEnum, java.lang.String)
     */
    @Override
    public Long increment(CacheEnum cache, String suffixKey) throws CustomerException {
        return increment(getKey(cache, suffixKey));
    }

    private Long increment(String key) {
        try {
            final Jedis jedis = get();
            return jedis.incr(key);
        } catch (Exception e) {
            throw new CustomerException("get increment atomic long error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#increment(com.eigpay.common.cache.enums.CacheEnum, long)
     */
    @Override
    public Long increment(CacheEnum cache, long step) throws CustomerException {
        return increment(cache.getCode(), step);
    }

    private Long increment(String key, long step) {
        try {
            final Jedis jedis = get();
            return jedis.incrBy(key, step);
        } catch (Exception e) {
            throw new CustomerException("get increment atomic long error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#increment(com.eigpay.common.cache.enums.CacheEnum, java.lang.String, long)
     */
    @Override
    public Long increment(CacheEnum cache, String suffixKey, long step) throws CustomerException {
        return increment(getKey(cache, suffixKey), step);
    }

    /**
     * @see com.eigpay.common.cache.CacheService#expire(com.eigpay.common.cache.enums.CacheEnum)
     */
    @Override
    public void expire(CacheEnum cache, int expire) throws CustomerException {
        expire(cache.getCode(), expire);
    }

    private void expire(String key, int expire) {
        try {
            final Jedis jedis = get();
            if (expire > 0) {
                jedis.expire(key, expire);
            }
        } catch (Exception e) {
            throw new CustomerException("put cache data to redis error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#expire(com.eigpay.common.cache.enums.CacheEnum, java.lang.String)
     */
    @Override
    public void expire(CacheEnum cache, String suffixKey, int expire) throws CustomerException {
        expire(getKey(cache, suffixKey), expire);
    }

    /**
     * @see com.eigpay.common.cache.CacheService#putWithExpire(com.eigpay.common.cache.enums.CacheEnum, java.lang.Object, int)
     */
    @Override
    public void putWithExpire(CacheEnum cache, Serializable value, int expire) {
        putWithExpire(cache.getCode(), value, expire);
    }

    private void putWithExpire(String key, Serializable value, int expire) {

        AssertUtil.notNull(value, "put cache data is null");

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
            throw new CustomerException("put cache data to redis error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#putWithExpire(com.eigpay.common.cache.enums.CacheEnum, java.lang.String, java.lang.Object, int)
     */
    @Override
    public void putWithExpire(CacheEnum cache, String suffixKey, Serializable value, int expire) {
        putWithExpire(getKey(cache, suffixKey), value, expire);
    }

    /**
     * @see com.eigpay.common.cache.CacheService#putPersisted(com.eigpay.common.cache.enums.CacheEnum, java.lang.Object)
     */
    @Override
    public void putPersisted(CacheEnum cache, Serializable value) {
        putPersisted(cache.getCode(), value);
    }

    private void putPersisted(String key, Serializable value) {

        AssertUtil.notNull(value, "put cache data is null");

        try {
            long startTime = System.currentTimeMillis();
            System.out.println("----Jedis get resource from pool start----"+new Date());
            final Jedis jedis = get();
            if (isSimpleObject(value)) {
                jedis.set(key, String.valueOf(value));
            } else {
                jedis.set(key.getBytes(ENCODING), SerializationUtils.serialize(value));
            }
            long endTime = System.currentTimeMillis();
            System.out.println("----Jedis get resource from pool end----"+new Date());
            System.out.println("----Jedis get resource from pool: use "+(endTime-startTime)/1000+" s----");

        } catch (Exception e) {
            throw new CustomerException("put cache data to redis error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#putPersisted(com.eigpay.common.cache.enums.CacheEnum, java.lang.String, java.lang.Object)
     */
    @Override
    public void putPersisted(CacheEnum cache, String suffixKey, Serializable value) {
        putPersisted(getKey(cache, suffixKey), value);
    }

    /**
     * @see com.eigpay.common.cache.CacheService#get(com.eigpay.common.cache.enums.CacheEnum)
     */
    @Override
    public <T> T get(CacheEnum cache) {
        return get(cache.getCode());
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String key) {

        AssertUtil.notBlank(key, "data cache key is null");

        try {
            final Jedis jedis = get();

            final byte[] datas = jedis.get(key.getBytes(ENCODING));
            if (ArrayUtils.isEmpty(datas)) {
                return null;
            }
            return SerializationUtils.deserialize(datas);
        } catch (SerializationException se) {
            return (T) getString(key);
        } catch (Exception e) {
            throw new CustomerException("get data from cache error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    private boolean isSimpleObject(Serializable value) {
        final Class<?> clazz = value.getClass();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz);
    }

    private String getString(String key) {
        try {
            return get().get(key);
        } catch (Exception e) {
            throw new CustomerException("get string data from cache error:" + e.getMessage(), e);
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#get(com.eigpay.common.cache.enums.CacheEnum, java.lang.String)
     */
    @Override
    public <T> T get(CacheEnum cache, String suffixKey) {
        return get(getKey(cache, suffixKey));
    }

    /**
     * @see com.eigpay.common.cache.CacheService#remove(com.eigpay.common.cache.enums.CacheEnum)
     */
    @Override
    public void remove(CacheEnum cache) {
        remove(cache.getCode());
    }

    private void remove(String key) {
        try {
            final Jedis jedis = get();
            jedis.del(key);
        } catch (Exception e) {
            throw new CustomerException("delete redis key error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * @see com.eigpay.common.cache.CacheService#remove(com.eigpay.common.cache.enums.CacheEnum, java.lang.String)
     */
    @Override
    public void remove(CacheEnum cache, String suffixKey) {
        remove(getKey(cache, suffixKey));
    }

    @Override
    public long putWithExpireIfNotExist(CacheEnum cache, Serializable value, int expire) throws CustomerException {
        return putWithExpireIfNotExist(cache.getCode(), value, expire);
    }

    @Override
    public long putWithExpireIfNotExist(CacheEnum cache, String suffixKey, Serializable value, int expire) throws CustomerException {
        return putWithExpireIfNotExist(getKey(cache, suffixKey), value, expire);
    }

    @Override
    public long putPersistedIfNotExist(CacheEnum cache, Serializable value) throws CustomerException {
        return putPersistedIfNotExist(cache.getCode(), value);
    }

    @Override
    public long putPersistedIfNotExist(CacheEnum cache, String suffixKey, Serializable value) throws CustomerException {
        return putPersistedIfNotExist(getKey(cache, suffixKey), value);
    }

    private long putPersistedIfNotExist(String key, Serializable value) {

        AssertUtil.notNull(value, "put cache data is null");
        long flag = 0L;
        try {
            final Jedis jedis = get();
            if (isSimpleObject(value)) {
                flag =jedis.setnx(key, String.valueOf(value));
            } else {
                flag =jedis.setnx(key.getBytes(ENCODING), SerializationUtils.serialize(value));
            }
        } catch (Exception e) {
            throw new CustomerException("put cache data to redis error:" + e.getMessage(), e);
        } finally {
            close();
        }
        return flag;
    }

    private long putWithExpireIfNotExist(String key, Serializable value, int expire) {

        AssertUtil.notNull(value, "put cache data is null");
        long flag = 0L;
        try {
            final Jedis jedis = get();
            if (isSimpleObject(value)) {
                flag = jedis.setnx(key, String.valueOf(value));
            } else {
                flag = jedis.setnx(key.getBytes(ENCODING), SerializationUtils.serialize(value));
            }
            if (expire > 0) {
                jedis.expire(key, expire);
            }
        } catch (Exception e) {
            throw new CustomerException("put cache data to redis error:" + e.getMessage(), e);
        } finally {
            close();
        }
        return flag;
    }

    /**
     * pipeline get
     *
     * @param cache
     * @param suffixKeys
     * @return
     * @throws CustomerException
     */
    @Override
    public List<String> batchGet(CacheEnum cache, final List<String> suffixKeys) throws CustomerException {
        AssertUtil.notNull(suffixKeys, "Redis batchGet suffixKeys is null");

        try {
            final Jedis jedis = get();
            List<String> resList = new ArrayList<String>();

            if (suffixKeys != null && suffixKeys.size() > 0) {
                Pipeline pipeline = jedis.pipelined();
                List<Response<String>> responseList = new ArrayList<Response<String>>();
                for (String suffixKey : suffixKeys) {
                    responseList.add(pipeline.get(getKey(cache, suffixKey)));
                }
                pipeline.sync();
                for (Response<String> entry : responseList) {
                    resList.add(entry.get());
                }
            }

            return resList;
        } catch (Exception e) {
            throw new CustomerException("Redis batchGet operation error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }


    /**
     * pipeline set
     *
     * @param cache
     * @param pairs
     * @throws CustomerException
     */
    @Override
    public void batchSet(CacheEnum cache, final List<Pair> pairs) throws CustomerException {
        AssertUtil.notNull(pairs, "Redis batchSet pairs is null");

        try {
            final Jedis jedis = get();

            if (pairs != null) {
                Pipeline pipeline = jedis.pipelined();

                while (pairs.size() > 0) {
                    List<Pair> batchPairs = pairs.size() > PIPE_LIMIT ? pairs.subList(0, PIPE_LIMIT) : pairs;

                    for (Pair pair : batchPairs) {
                        pipeline.set(getKey(cache, pair.getKey()), pair.getValue());
                    }
                    pipeline.sync();
                    batchPairs.clear();
                }
            }
        } catch (Exception e) {
            throw new CustomerException("Redis batchSet operation error:" + e.getMessage(), e);
        } finally {
            close();
        }
    }

    /**
     * pipeline remove
     *
     * @param cache
     * @param suffixKeys
     * @throws CustomerException
     */
    @Override
    public void batchDel(CacheEnum cache, final List<String> suffixKeys) throws CustomerException {
        AssertUtil.notNull(suffixKeys, "Redis batchDel suffixKeys is null");

        try {
            final Jedis jedis = get();
            Pipeline pipeline = jedis.pipelined();

            if (suffixKeys != null) {
                while (suffixKeys.size() > 0) {
                    List<String> batchSuffixKeys = suffixKeys.size() > PIPE_LIMIT ? suffixKeys.subList(0, PIPE_LIMIT) : suffixKeys;

                    for (String key : batchSuffixKeys) {
                        pipeline.del(getKey(cache, key));
                    }
                    pipeline.sync();
                    batchSuffixKeys.clear();
                }
            }
        } catch (Exception e) {
            throw new CustomerException("Redis batchDel operation error:" + e.getMessage(), e);
        } finally {
            close();
        }
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
