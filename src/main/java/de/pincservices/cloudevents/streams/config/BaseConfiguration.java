package de.pincservices.cloudevents.streams.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.pincservices.cloudevents.streams.service.Converter;

@Configuration
public class BaseConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public Converter converter(ObjectMapper objectMapper) {
        return new Converter(objectMapper);
    }
}
