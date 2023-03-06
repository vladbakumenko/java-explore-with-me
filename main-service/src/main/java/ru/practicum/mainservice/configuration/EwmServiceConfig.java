package ru.practicum.mainservice.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.StatClient;

@Configuration
public class EwmServiceConfig {

    @Bean
    public StatClient statClient() {
        return new StatClient("http://localhost:9090", new RestTemplateBuilder());
    }
}
