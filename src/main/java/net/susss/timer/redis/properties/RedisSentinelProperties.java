package net.susss.timer.redis.properties;

import lombok.Data;
import lombok.ToString;

/**
 * Created by Suki Yang on 11/1/2018.
 */
@Data
@ToString
public class RedisSentinelProperties {

    /**
     * 哨兵master 名称
     */
    private String master;

    /**
     * 哨兵节点
     */
    private String nodes;

    /**
     * 哨兵配置
     */
    private boolean masterOnlyWrite;

    /**
     *
     */
    private int failMax;
}
