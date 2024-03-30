package de.pincservices.cloudevents.streams.config;

import java.net.URI;
import java.util.function.Function;

import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class EventFunctions {

    @Bean
    public Function<Message<CloudEvent>, Message<CloudEvent>> processEvent() {
        return message -> {
            log.info("Process Cloud Event: {}", message.getPayload());
            CloudEvent newEvent = CloudEventBuilder.v1()
                    .withData(message.getPayload().getData())
                    .withId(message.getPayload().getId())
                    .withSource(URI.create("http://localhost/processEvent"))
                    .withType("process.example")
                    .build();
            return CloudEventMessageBuilder.withData(newEvent).build();
        };
    }

/*
    @Bean
    public Supplier<Message<CloudEvent>> supplyEvent() {
        return () -> {
            CloudEvent event =  CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withSource(URI.create("http://localhost/junitTest"))
                    .withType("supplier.example")
                    .withData("plain/text", "TestText".getBytes(StandardCharsets.UTF_8))
                    .build();
            log.info("Supply event: {}", event);
            return CloudEventMessageBuilder.withData(event).build();
        };
    }

    @Bean
    public Consumer<CloudEvent> consumeEvent() {
        return event -> log.info("Consumed Cloud Event: {}", event);
    }

 */
}
