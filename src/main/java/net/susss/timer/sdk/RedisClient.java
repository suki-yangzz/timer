package net.susss.timer.sdk;

import net.susss.timer.redis.properties.RedisProperties;
import net.susss.timer.redisson.TiRedisson;
import net.susss.timer.api.TiRedissonClient;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Suki Yang on 11/1/2018.
 */
public class RedisClient {

    @Autowired
    RedisProperties redisProperties;

    public TiRedissonClient redissonSingle(String address, int connTimeout, int poolSize, int poolMinIdleSize, String password) {
        Config config = new Config();
        address = address.startsWith("redis://") ? address : "redis://" + address;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setTimeout(connTimeout)
                .setConnectionPoolSize(poolSize)
                .setConnectionMinimumIdleSize(poolMinIdleSize);
        if (StringUtils.isNotBlank(password)) {
            serverConfig.setPassword(password);
        }
        return TiRedisson.createTiRedissonClient(config);
    }

    public RedissonClient redissonCluster() {
        System.out.println("cluster redisProperties:" + redisProperties.getCluster());

        Config config = new Config();
        String[] nodes = redisProperties.getCluster().getNodes().split(",");
        List<String> newNodes = new ArrayList(nodes.length);
        Arrays.stream(nodes).forEach((index) -> newNodes.add(
                index.startsWith("redis://") ? index : "redis://" + index));

        ClusterServersConfig serverConfig = config.useClusterServers()
                .addNodeAddress(newNodes.toArray(new String[0]))
                .setScanInterval(
                        redisProperties.getCluster().getScanInterval())
                .setIdleConnectionTimeout(
                        redisProperties.getPool().getSoTimeout())
                .setConnectTimeout(
                        redisProperties.getPool().getConnTimeout())
                .setFailedAttempts(
                        redisProperties.getCluster().getFailedAttempts())
                .setRetryAttempts(
                        redisProperties.getCluster().getRetryAttempts())
                .setRetryInterval(
                        redisProperties.getCluster().getRetryInterval())
                .setMasterConnectionPoolSize(redisProperties.getCluster()
                        .getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redisProperties.getCluster()
                        .getSlaveConnectionPoolSize())
                .setTimeout(redisProperties.getTimeout());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    public RedissonClient redissonSentinel() {
        System.out.println("sentinel redisProperties:" + redisProperties.getSentinel());
        Config config = new Config();
        String[] nodes = redisProperties.getSentinel().getNodes().split(",");
        List<String> newNodes = new ArrayList(nodes.length);
        Arrays.stream(nodes).forEach((index) -> newNodes.add(
                index.startsWith("redis://") ? index : "redis://" + index));

        SentinelServersConfig serverConfig = config.useSentinelServers()
                .addSentinelAddress(newNodes.toArray(new String[0]))
                .setMasterName(redisProperties.getSentinel().getMaster())
                .setReadMode(ReadMode.SLAVE)
                .setFailedAttempts(redisProperties.getSentinel().getFailMax())
                .setTimeout(redisProperties.getTimeout())
                .setMasterConnectionPoolSize(redisProperties.getPool().getSize())
                .setSlaveConnectionPoolSize(redisProperties.getPool().getSize());

        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }
}
