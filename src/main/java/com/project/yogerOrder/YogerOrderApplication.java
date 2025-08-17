package com.project.yogerOrder;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.project.yogerOrder.global.config.TimeZoneInitializer;

@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S", defaultLockAtLeastFor = "PT10S")
@SpringBootApplication
@ConfigurationPropertiesScan
public class YogerOrderApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(YogerOrderApplication.class)
			.initializers(new TimeZoneInitializer())
			.run(args);
	}

}
