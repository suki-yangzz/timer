package net.susss.timer.api;

import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 10/31/2018.
 */
public interface Timer {

    void init();

    void set(String key, String value, long startTime, TimeUnit timeUnit, long timeout);

    void unset(String key, long startTime, TimeUnit timeUnit, long timeout);

}
