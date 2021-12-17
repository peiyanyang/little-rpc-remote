package com.learn.more.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author peiyy
 * @version 1.0
 * @date 2021-03-15 14:43
 */


@SpringBootApplication
@ComponentScan(basePackages = "com.learn.more,com.learn.more.server")
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}
}