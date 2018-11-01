package net.susss.timer.example;

import net.susss.timer.sdk.Reporter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Suki Yang on 10/24/2018.
 */
public class ReporterTest {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:/spring/applicationContext.xml"});

        Reporter reporter = (Reporter)context.getBean("reporter");

        System.out.println(reporter.getExecutor().toString());

        context.close();
    }
}
