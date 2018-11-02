package net.susss.timer.example;

import net.susss.timer.sdk.Agent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Suki Yang on 10/24/2018.
 */
public class AgentTest {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:/spring/applicationContext-agent.xml"});

        Agent agent = (Agent)context.getBean("agent");

        context.close();


    }
}
