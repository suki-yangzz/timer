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
     * put cache with expire, cache key is cache enum code, Support any type of data
     * @param cache cache enum
     * @param value Data to be stored
     * @param expire expire, unit: seconds
     */
    public void putWithExpire(CacheEnum cache, Serializable value, int expire);

    /**
     * get data from cache, cache key is cache enum code, The data type is the type of storage
     * @param cache cache enum
     */
    public <T> T get(CacheEnum cache);

    /**
     * get data from cache, cache key = (cache enum code + suffixKey), The data type is the type of storage
     * @param cache cache enum
     * @param suffixKey suffix key
     */
    public <T> T get(CacheEnum cache, String suffixKey);

    /**
     * remove data from cache, cache key is cache enum code
     * @param cache cache enum
     */
    public void remove(CacheEnum cache);

    /**
     * remove data from cache, cache key = (cache enum code + suffixKey)
     * @param cache cache enum
     * @param suffixKey suffix key
     */
    public void remove(CacheEnum cache, String suffixKey);

}
