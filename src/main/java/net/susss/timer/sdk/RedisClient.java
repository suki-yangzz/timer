package net.susss.timer.sdk;

import net.susss.timer.redisson.TiRedisson;
import net.susss.timer.api.TiRedissonClient;
import org.apache.commons.lang3.StringUtils;
import org.redisson.config.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Suki Yang on 11/1/2018.
 */
public class RedisClient {

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

    public TiRedissonClient redissonMasterSlave(String master, String slave, int connTimeout, String password) {
        master = master.startsWith("redis://") ? master : "redis://" + master;
        String[] slaveAddresses = slave.split(",");
        List<String> slaves = new ArrayList(slaveAddresses.length);
        Arrays.stream(slaveAddresses).forEach((index) -> slaves.add(
                index.startsWith("redis://") ? index : "redis://" + index));
        System.out.println(slaves.toArray(new String[0]));
        Config config = new Config();
        MasterSlaveServersConfig serverConfig = config.useMasterSlaveServers()
                .setMasterAddress(master)
                .addSlaveAddress(slaves.toArray(new String[0]))
                .setTimeout(connTimeout);
        if (StringUtils.isNotBlank(password)) {
            serverConfig.setPassword(password);
        }
        return TiRedisson.createTiRedissonClient(config);
    }
}
