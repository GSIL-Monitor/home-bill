package com.hb.web.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@ComponentScan(value = { "com.hb" })
@EnableAutoConfiguration
@ServletComponentScan
//@EnableTransactionManagement
//@EnableScheduling
public class Application implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	
	//private static String[] args1 = new String[]{"--spring.config.location=路径/conf-resources/conf/","--spring.profiles.active=dev"};
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public void run(String... arg0) throws Exception {
		StringBuilder sb=new StringBuilder();
		for(String s:arg0) {
			sb.append(s).append(" ");
		}
		logger.info(sb.toString());
	}
}
