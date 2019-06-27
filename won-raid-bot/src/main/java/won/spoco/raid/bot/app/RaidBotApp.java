package won.spoco.raid.bot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class RaidBotApp {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(
                new Object[]{"classpath:/spring/app/raidBotApp.xml"}
        );
        app.setWebEnvironment(false);
        ConfigurableApplicationContext applicationContext =  app.run(args);
        //Thread.sleep(5*60*1000);
        //app.exit(applicationContext);
    }
}
