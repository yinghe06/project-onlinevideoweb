package com.imooc;

import com.imooc.bilibili.service.util.RSAUtil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class imoocBilibiliApp {
    public static void main(String[] args) throws Exception {
        ApplicationContext app = SpringApplication.run(imoocBilibiliApp.class, args);

        String encrypt = RSAUtil.encrypt("123456");
        System.out.println(encrypt);
    }
}
