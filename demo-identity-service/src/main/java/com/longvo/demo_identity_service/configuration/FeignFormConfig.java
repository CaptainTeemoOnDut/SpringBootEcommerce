package com.longvo.demo_identity_service.configuration;

import feign.Logger;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignFormConfig {

    private final ObjectFactory<HttpMessageConverters> messageConverters;

    public FeignFormConfig(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Bean
    public Encoder feignFormEncoder() {
        System.out.println(">>> USING FORM ENCODER <<<");
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

