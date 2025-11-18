package com.project.yogerOrder.global.config;

import java.lang.management.ManagementFactory;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import jdk.management.VirtualThreadSchedulerMXBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.java21.instrument.binder.jdk.VirtualThreadMetrics;

@Configuration
public class MicrometerConfig {

    @Bean
    public VirtualThreadMetrics virtualThreadMetrics () {
        return new VirtualThreadMetrics();
    }

    @Bean
    public MeterBinder virtualThreadSchedulerMXBean() {
        return (registry) -> {
            VirtualThreadSchedulerMXBean platformMXBean = ManagementFactory
                    .getPlatformMXBean(VirtualThreadSchedulerMXBean.class);

            if (platformMXBean == null) {
                throw new IllegalStateException("VirtualThreadSchedulerMXBean is not available. Ensure that the application is running on a Java version that supports virtual threads.");
            }

            Gauge.builder("jvm.threads.virtual.parallelism", platformMXBean, VirtualThreadSchedulerMXBean::getParallelism)
                    .description("Virtual thread scheduler's target parallelism")
                    .register(registry);

            Gauge.builder("jvm.threads.virtual.pool.size", platformMXBean, VirtualThreadSchedulerMXBean::getPoolSize)
                    .description("Current number of platform threads in the scheduler pool")
                    .register(registry);

            Gauge.builder("jvm.threads.virtual.mounted", platformMXBean, VirtualThreadSchedulerMXBean::getMountedVirtualThreadCount)
                    .description("Approximate current number of virtual threads that are mounted")
                    .register(registry);

            Gauge.builder("jvm.threads.virtual.queued", platformMXBean, VirtualThreadSchedulerMXBean::getQueuedVirtualThreadCount)
                    .description("Approximate current number of virtual threads that are queued")
                    .register(registry);
        };

    }
}
