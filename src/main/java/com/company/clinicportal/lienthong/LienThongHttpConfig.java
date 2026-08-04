package com.company.clinicportal.lienthong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LienThongProperties.class)
public class LienThongHttpConfig {

    @Bean
    public RestClient lienThongRestClient(LienThongProperties props, MappingJackson2HttpMessageConverter lienThongConverter) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) Duration.ofMillis(props.getConnectTimeoutMs()).toMillis());
        rf.setReadTimeout((int) Duration.ofMillis(props.getReadTimeoutMs()).toMillis());
        return RestClient.builder()
                .baseUrl(props.getApiBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .messageConverters(c -> { c.clear(); c.add(lienThongConverter); })
                .requestFactory(rf)
                .build();
    }

    @Bean(name = "lienThongObjectMapper")
    public ObjectMapper lienThongObjectMapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // BYT API dùng snake_case: loai_don_thuoc, ma_don_thuoc, ngay_sinh_benh_nhan...
        m.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return m;
    }

    @Bean(name = "lienThongJacksonConverter")
    public MappingJackson2HttpMessageConverter lienThongJacksonConverter() {
        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(lienThongObjectMapper());
        c.setSupportedMediaTypes(java.util.List.of(MediaType.APPLICATION_JSON));
        return c;
    }
}
