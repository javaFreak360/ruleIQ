package com.highcourt.ruleIQ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class RuleIqApplication {
	private static final Logger logger = LoggerFactory.getLogger(RuleIqApplication.class);

	public static void main(String[] args) {
		logger.info("Starting RuleIQ..");
		SpringApplication.run(RuleIqApplication.class, args);
	}

}
