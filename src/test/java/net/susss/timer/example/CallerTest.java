package net.susss.timer.example;

import net.susss.timer.sdk.Reporter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Suki Yang on 10/24/2018.
 */
public class CallerTest {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"src/test/resources/spring/applicationContext.xml"});

        Reporter reporter = (Reporter)context.getBean("reporter");

        System.out.println(reporter);

        context.close();
    }
}
