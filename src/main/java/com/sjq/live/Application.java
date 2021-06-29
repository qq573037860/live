package com.sjq.live;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);

        //非web方式启动
        String webApplicationType = applicationContext.getEnvironment().getProperty("spring.main.web-application-type");
        if (StringUtils.equals("none", webApplicationType)) {
            Thread.currentThread().join();
        }
    }

}