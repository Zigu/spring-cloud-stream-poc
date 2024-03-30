package de.pincservices.cloudevents.streams.config;

import java.net.URI;
import java.util.function.Function;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StreamFunctions {

    @Bean
    public Function<KStream<String, CloudEvent>, KStream<String, CloudEvent>> processEventStream() {
        return stream -> stream.mapValues(value -> {
            log.info("Process Cloud Event Stream: {}", value);
            return CloudEventBuilder.v1()
                    .withData(value.getData())
                    .withId(value.getId())
                    .withSource(URI.create("http://localhost/processEventStream"))
                    .withType("process.stream.example")
                    .build();
        });
    }


}
