package com.project.yogerOrder.global.config;

import org.library.yogerLibrary.log.LogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogFilterConfig {

    @Bean
    public FilterRegistrationBean<LogFilter> myCustomFilter() {
        FilterRegistrationBean<LogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LogFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(-101); // SecurityFilterChain보다 앞에 실행
        return registrationBean;
    }
}
