package net.susss.timer.redisson;

import org.redisson.QueueTransferService;
import org.redisson.RedissonDelayedQueue;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * Created by Suki Yang on 11/2/2018.
 */
public class TiRedissonDelayedQueue<V> extends RedissonDelayedQueue<V> {

    protected TiRedissonDelayedQueue(QueueTransferService queueTransferService, Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        super(queueTransferService, codec, commandExecutor, name);
    }
}
