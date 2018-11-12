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

    public RFuture<V> checkTimeoutAsync() {
        return this.commandExecutor.evalWriteAsync(this.getName(), this.codec, RedisCommands.EVAL_OBJECT,
                "local v = redis.call('rpop', KEYS[1]); " +
                    "if v ~= false then " +
                        "local bucket = string.match(v, '\"(.+)%%'); " +
                        "local key = string.match(v, '%%(.+)\"'); " +
                        "local score = redis.call('zscore', bucket, '\\\"'..key..'\\\"'); " +
                        "if score ~= false then " +
                            "local startTime = string.match(v, '%%(.+)_'); " +
                            "redis.call('zadd', KEYS[2], startTime, v); " + //identify a timeout, then add into timeout_set
                            "redis.call('zrem', bucket, '\\\"'..key..'\\\"')" + //remove element from related bucket
                            "return v; " +
                        "end; " +
                    "end; " +
                    "return nil; ",
                Arrays.<Object>asList(Constants.DELAY_QUEUE, Constants.TIMEOUT_SET));
    }

    public V checkTimeout() throws InterruptedException {
        return this.get(this.checkTimeoutAsync());
    }
}
