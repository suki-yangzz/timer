package net.susss.timer.redis.properties;

import lombok.Data;
import lombok.ToString;

/**
 * Created by Suki Yang on 11/1/2018.
 */
@Data
@ToString
public class RedisPoolProperties {

    private int maxIdle;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int connTimeout;

    private int soTimeout;

    /**
     * 池大小
     */
    private  int size;

}
