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
    private String teamServiceUrl;

    @Bean
    public RestTemplate userServiceRestTemplate() {
        return initService(userServiceUrl);
    }

    @Bean
    public RestTemplate teamServiceRestTemplate() {
        return initService(teamServiceUrl);
    }

    private RestTemplate initService(final String url) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(url));
        return restTemplate;
    }
}