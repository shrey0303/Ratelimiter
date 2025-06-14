package com.ratemaster.overseer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ratelimiter.config.HierarchicalRateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HierarchicalRateLimitProperties.class)
public class RateLimitingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateLimitingApiApplication.class, args);
	}

}
