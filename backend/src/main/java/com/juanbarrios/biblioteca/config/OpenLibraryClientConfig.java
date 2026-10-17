package com.juanbarrios.biblioteca.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenLibraryClientConfig {

    @Bean
    public RestClient openLibraryRestClient(
            @Value("${app.openlibrary.base-url}") String baseUrl,
            @Value("${app.openlibrary.timeout-seconds}") long timeoutSeconds) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(timeoutSeconds))
                .withReadTimeout(Duration.ofSeconds(timeoutSeconds));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
