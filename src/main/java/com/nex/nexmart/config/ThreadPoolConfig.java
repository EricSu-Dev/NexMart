package com.nex.nexmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
	@Bean("nexmartExecutor")
	public ThreadPoolExecutor nexmartExecutor() {
		return new ThreadPoolExecutor(
				17,//IO密集任务用内核数*2+1
				32,
				60L, TimeUnit.SECONDS,
				//无OOM风险
				new ArrayBlockingQueue<>(500),
				//Lambda 实现 ThreadFactory 接口,更改线程名称格式
				r -> {
					Thread t = new Thread(r);
					t.setName("nexmart-pool-" + t.getId());
					return t;
				},
				new ThreadPoolExecutor.CallerRunsPolicy()
		);
	}
}
