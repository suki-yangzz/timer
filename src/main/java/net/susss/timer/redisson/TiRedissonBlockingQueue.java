package net.susss.timer.redisson;

import net.susss.timer.api.TiRedissonClient;
import net.susss.timer.common.Constants;
import org.redisson.RedissonBlockingQueue;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Arrays;

/**
 * Created by Suki Yang on 11/2/2018.
 */
public class TiRedissonBlockingQueue<V> extends RedissonBlockingQueue<V> {

    public TiRedissonBlockingQueue(CommandAsyncExecutor commandExecutor, String name, TiRedissonClient redisson) {
        super(commandExecutor, name, redisson);
    }

    public TiRedissonBlockingQueue(Codec codec, CommandAsyncExecutor commandExecutor, String name, TiRedissonClient redisson) {
        super(codec, commandExecutor, name, redisson);
    }

    public RFuture<V> takeAsyncReliably() {
        System.out.println("take async reliably");
//        return this.commandExecutor.writeAsync(this.getName(), this.codec, RedisCommands.RPOPLPUSH, this.getName(), Constants.RELIABLE_QUEUE);
        return this.commandExecutor.evalWriteAsync(this.getName(), this.codec, RedisCommands.EVAL_VOID,
                "local v = redis.call('SET', 'test_key', 'test_value'); ",
                Arrays.asList(new Object[]{"test"}));
    }

    public V takeReliably() throws InterruptedException {
        return this.get(this.takeAsyncReliably());
    }
}
