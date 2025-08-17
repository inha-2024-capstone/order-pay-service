package com.project.yogerOrder.global.config;

import java.util.TimeZone;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TimeZoneInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	
	@Override
	public void initialize(ConfigurableApplicationContext ctx) {
		String tz = ctx.getEnvironment().getProperty("global.timeZone");
		if (tz != null) TimeZone.setDefault(TimeZone.getTimeZone(tz));
	}
	
}
