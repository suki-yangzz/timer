package net.susss.timer;

import net.susss.timer.sdk.Caller;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 10/24/2018.
 */
@Service("Caller")
public class CallerImpl implements Caller {

    public void set(String key, String value, TimeUnit timeUnit, long timeout) {

    }

    public void unset(String key) {

    }
}
