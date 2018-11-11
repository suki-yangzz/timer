package net.susss.timer.example;

import net.susss.timer.api.Timer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suki Yang on 11/1/2018.
 */
public class MainTest {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:/spring/applicationContext-main.xml"});

        Timer timer = (Timer) context.getBean("timer");

        long unset_startTime = new Date().getTime();
        timer.set("key1", "value1", unset_startTime, TimeUnit.SECONDS, 10);
        timer.set("key2", "value2", new Date().getTime(), TimeUnit.SECONDS, 20);
        timer.set("key3", "value3", unset_startTime, TimeUnit.SECONDS, 30);
        timer.set("key4", "value4", new Date().getTime(), TimeUnit.SECONDS, 40);

        timer.unset("key1", unset_startTime, TimeUnit.SECONDS, 10);
        timer.unset("key3", unset_startTime, TimeUnit.SECONDS, 30);
    }
}
