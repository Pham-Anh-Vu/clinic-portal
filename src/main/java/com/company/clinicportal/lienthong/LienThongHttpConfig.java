package com.company.clinicportal.lienthong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LienThongProperties.class)
public class LienThongHttpConfig {

    @Bean
    public RestClient lienThongRestClient(LienThongProperties props) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) Duration.ofMillis(props.getConnectTimeoutMs()).toMillis());
        rf.setReadTimeout((int) Duration.ofMillis(props.getReadTimeoutMs()).toMillis());
        return RestClient.builder()
                .baseUrl(props.getApiBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(rf)
                .build();
    }

    @Bean(name = "lienThongObjectMapper")
    public ObjectMapper lienThongObjectMapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return m;
    }
}
