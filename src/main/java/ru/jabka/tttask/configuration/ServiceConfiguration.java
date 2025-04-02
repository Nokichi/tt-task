package ru.jabka.tttask.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Setter
@Configuration
@ConfigurationProperties("services")
public class ServiceConfiguration {

    private String userServiceUrl;

    @Bean
    public RestTemplate userService() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(userServiceUrl));
        return restTemplate;
    }
}