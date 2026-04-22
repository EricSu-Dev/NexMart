package com.nex.nexmart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.nex.nexmart.mapper")
public class NexMartApplication {
	public static void main(String[] args) {
		SpringApplication.run(NexMartApplication.class, args);
	}
}
