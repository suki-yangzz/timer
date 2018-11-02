package net.susss.timer.redisson;

import net.susss.timer.api.TiRedissonClient;
import org.redisson.Redisson;
import org.redisson.config.Config;

/**
 * Created by Suki Yang on 11/2/2018.
 */
public class TiRedisson extends Redisson implements TiRedissonClient {

    protected TiRedisson(Config config) {
        super(config);
    }

    public static TiRedissonClient createTiRedissonClient(Config config) {
        TiRedisson redisson = new TiRedisson(config);
        if(config.isReferenceEnabled()) {
            redisson.enableRedissonReferenceSupport();
        }

        return redisson;
    }

    @Override
    public TiRedissonBlockingQueue<String> getTiBlockingQueue(String name) {
        return new TiRedissonBlockingQueue(this.connectionManager.getCommandExecutor(), name, this);
    }

    @Override
    public TiRedissonDelayedQueue<String> getTiDelayedQueue(TiRedissonBlockingQueue<String> destinationQueue) {
        if(destinationQueue == null) {
            throw new NullPointerException();
        } else {
            return new TiRedissonDelayedQueue(this.queueTransferService, destinationQueue.getCodec(), this.connectionManager.getCommandExecutor(), destinationQueue.getName());
        }
    }
}
