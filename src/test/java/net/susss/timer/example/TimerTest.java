package net.susss.timer.example;

import net.susss.timer.sdk.Timer;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 11/12/2018.
 */
@ContextConfiguration(locations={"classpath*:/spring/applicationContext-main.xml"})
public class TimerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private Timer timer;

    @Test
    public void timerTest() throws InterruptedException {
        long unset_startTime = new Date().getTime();
        timer.set("key1", "value1", unset_startTime, TimeUnit.SECONDS, 10);
        timer.set("key2", "value2", new Date().getTime(), TimeUnit.SECONDS, 20);
        timer.set("key3", "value3", unset_startTime, TimeUnit.SECONDS, 30);
        timer.set("key4", "value4", new Date().getTime(), TimeUnit.SECONDS, 40);

        timer.unset("key1", unset_startTime, TimeUnit.SECONDS, 10);
        timer.unset("key3", unset_startTime, TimeUnit.SECONDS, 30);

        Thread.sleep(10*1000);

        System.out.println(timer.handle());//expected key2
    }
}
