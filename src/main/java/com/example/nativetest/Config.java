package com.example.nativetest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Config {

    @Bean
    public SampleBean myBean() {
        return new SampleBean();
    }
}
