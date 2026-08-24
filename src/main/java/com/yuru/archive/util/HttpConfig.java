package com.yuru.archive.util;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yuru.archive.linkpreview.dto.OgDto;

import reactor.netty.http.client.HttpClient;

@Configuration
public class HttpConfig {

    @Bean
    WebClient microlinkClient(
            @Value("${app.microlink.base-url:https://api.microlink.io}") String baseUrl,
            @Value("${app.microlink.timeout-ms:5000}") long timeoutMs) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeoutMs));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    Cache<String, OgDto> ogCache(@Value("${app.cache.og-ttl-min:10}") long ttlMinutes) {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .build();
    }
}
