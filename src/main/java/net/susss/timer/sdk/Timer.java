package net.susss.timer.sdk;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Suki Yang on 10/31/2018.
 */
public interface Timer {
    /**
     * class init
     */
    public void init();

    /**
     * increment atomic, step value is input parameter step
     * @param cache cache enum
     * @param step step
     */
    public Long increment(CacheEnum cache, long step) throws CustomerException;

    /**
     * increment atomic, step value is input parameter step
     * @param cache cache enum
     * @param suffixKey suffix key
     * @param step step
     */
    public Long increment(CacheEnum cache, String suffixKey, long step) throws CustomerException;

    /**
     * increment atomic, step value = 1
     * @param cache cache enum
     */
    public Long increment(CacheEnum cache) throws CustomerException;

    /**
     * increment atomic, step value = 1
     * @param cache cache enum
     * @param suffixKey suffix key
     */
    public Long increment(CacheEnum cache, String suffixKey) throws CustomerException;

    /**
     * expire cache data
     * @param cache cache enum
     * @param expire expire, unit: seconds
     */
    public void expire(CacheEnum cache, int expire) throws CustomerException;

    /**
     * expire cache data
     * @param cache cache enum
     * @param suffixKey suffix key
     * @param expire expire, unit: seconds
     */
    public void expire(CacheEnum cache, String suffixKey, int expire) throws CustomerException;

    /**
     * put cache with expire, cache key is cache enum code, Support any type of data
     * @param cache cache enum
     * @param value Data to be stored
     * @param expire expire, unit: seconds
     */
    public void putWithExpire(CacheEnum cache, Serializable value, int expire) throws CustomerException;

    /**
     * put cache with expire, cache key = (cache enum code + suffixKey), Support any type of data
     * @param cache cache enum
     * @param suffixKey suffix key
     * @param value Data to be stored
     * @param expire expire, unit: seconds
     */
    public void putWithExpire(CacheEnum cache, String suffixKey, Serializable value, int expire) throws CustomerException;


    /**
     * put cache with expire, cache key is cache enum code, Support any type of data<br>
     * but if the key is already exist, the operation will be failed.<br>
     * return 0 means key is already exist,put failed<br>
     * return 1 means put data succeed<br>
     * @param cache
     * @param value
     * @param expire
     * @return
     * @throws CustomerException
     */
    public long putWithExpireIfNotExist(CacheEnum cache, Serializable value, int expire)throws CustomerException;

    /**
     * put cache with expire, cache key = (cache enum code + suffixKey), Support any type of data
     * but if the key is already exist, the operation will be failed.<br>
     * return 0 means key is already exist,put failed<br>
     * return 1 means put data succeed<br>
     * @param cache
     * @param suffixKey
     * @param value
     * @param expire
     * @return
     * @throws CustomerException
     */
    public long putWithExpireIfNotExist(CacheEnum cache, String suffixKey, Serializable value, int expire)throws CustomerException;


    /**
     * put cache with persisted, cache key is cache enum code, Support any type of data, Never expire
     * @param cache cache enum
     * @param value Data to be stored
     */
    public void putPersisted(CacheEnum cache, Serializable value) throws CustomerException;

    /**
     * put cache with persisted, cache key = (cache enum code + suffixKey), Support any type of data, Never expire
     * @param cache cache enum
     * @param suffixKey suffix key
     * @param value Data to be stored
     */
    public void putPersisted(CacheEnum cache, String suffixKey, Serializable value) throws CustomerException;

    /**
     * put cache with persisted, cache key is cache enum code, Support any type of data, Never expire
     * but if the key is already exist, the operation will be failed.<br>
     * return 0 means key is already exist,put failed<br>
     * return 1 means put data succeed<br>
     * @param cache cache enum
     * @param value Data to be stored
     */
    public long putPersistedIfNotExist(CacheEnum cache, Serializable value)throws CustomerException;

    /**
     * put cache with persisted, cache key = (cache enum code + suffixKey), Support any type of data, Never expire
     * but if the key is already exist, the operation will be failed.<br>
     * return 0 means key is already exist,put failed<br>
     * return 1 means put data succeed<br>
     * @param cache cache enum
     * @param suffixKey suffix key
     * @param value Data to be stored
     */
    public long putPersistedIfNotExist(CacheEnum cache, String suffixKey, Serializable value)throws CustomerException;

    /**
     * get data from cache, cache key is cache enum code, The data type is the type of storage
     * @param cache cache enum
     */
    public <T> T get(CacheEnum cache) throws CustomerException;

    /**
     * get data from cache, cache key = (cache enum code + suffixKey), The data type is the type of storage
     * @param cache cache enum
     * @param suffixKey suffix key
     */
    public <T> T get(CacheEnum cache, String suffixKey) throws CustomerException;

    /**
     * remove data from cache, cache key is cache enum code
     * @param cache cache enum
     */
    public void remove(CacheEnum cache) throws CustomerException;

    /**
     * remove data from cache, cache key = (cache enum code + suffixKey)
     * @param cache cache enum
     * @param suffixKey suffix key
     */
    public void remove(CacheEnum cache, String suffixKey) throws CustomerException;

    /**
     *
     *
     * @param cache
     * @param suffixKeys
     * @return
     * @throws CustomerException
     */
    public List<String> batchGet(CacheEnum cache, final List<String> suffixKeys) throws CustomerException;
    /**
     *
     *
     * @param cache
     * @param pairs
     * @throws CustomerException
     */
    public void batchSet(CacheEnum cache, final List<com.eigpay.common.cache.redis.RedisCacheServiceImpl.Pair> pairs) throws CustomerException;

    /**
     *
     *
     * @param cache
     * @param suffixKeys
     * @throws CustomerException
     */
    public void batchDel(CacheEnum cache, final List<String> suffixKeys) throws CustomerException;
}
