package com.siemens.clock.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.io.IOException;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {


        ResponseErrorHandler nonThrowingErrorHandler = new DefaultResponseErrorHandler() {
            @Override
            // The crucial override: tell RestTemplate *not* to throw an exception
            // for any status code (including 4xx and 5xx).
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            // Note: We don't need to override handleError since hasError returns false.
        };


        return builder
                .errorHandler(nonThrowingErrorHandler)
                .build();
    }
}