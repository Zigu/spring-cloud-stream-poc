package de.pincservices.cloudevents.streams.config;

import java.net.URI;
import java.util.function.Function;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.pincservices.cloudevents.streams.service.Converter;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StreamFunctions {

    @Bean
    public Function<KStream<String, CloudEvent>, KStream<String, CloudEvent>> processEventStream(Converter converter) {
        return stream -> stream.mapValues(value -> {
            log.info("Process Cloud Event Stream: {}", value);

            return converter.convertStreamEvent(value, URI.create("http://localhost/processEventStream"));
        });
    }

}
