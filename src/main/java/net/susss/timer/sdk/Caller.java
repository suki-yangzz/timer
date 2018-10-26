package net.susss.timer.sdk;

import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 10/24/2018.
 */
public interface Caller {

    void set(String key, String value, TimeUnit timeUnit, long timeout);

    void unset(String key);
}
