package de.pincservices.cloudevents.streams.config;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.pincservices.cloudevents.streams.dto.SampleDTO;
import de.pincservices.cloudevents.streams.service.Converter;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class EventFunctions {

    @Bean
    public Function<Message<CloudEvent>, Message<CloudEvent>> processEvent(Converter converter) {
        return message -> {
            CloudEvent oldEvent = message.getPayload();

            log.info("Process Cloud Event: {}", oldEvent);

            CloudEvent newEvent = converter.convertEvent(oldEvent, URI.create("http://localhost/processEvent"));

            return CloudEventMessageBuilder.withData(newEvent).setHeader(KafkaHeaders.KEY, newEvent.getId()).build();
        };
    }


    @Bean
    public Supplier<Message<CloudEvent>> supplyEvent(ObjectMapper objectMapper) {
        return () -> {

            SampleDTO dto = new SampleDTO("TestText", Instant.now());

            try {
                final byte[] data = objectMapper.writeValueAsBytes(dto);

                CloudEvent event =  CloudEventBuilder.v1()
                        .withId(UUID.randomUUID().toString())
                        .withSource(URI.create("http://localhost/junitTest"))
                        .withType("SampleDTO_v1")
                        .withData(MimeTypeUtils.APPLICATION_JSON_VALUE, data)
                        .withoutDataSchema()
                        .build();

                log.info("Supply Cloud Event: {}", event);

                return CloudEventMessageBuilder.withData(event).setHeader(KafkaHeaders.KEY, event.getId()).build();

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        };
    }

    @Bean
    public Consumer<CloudEvent> consumeEvent() {
        return event -> log.info("Consume Cloud Event: {}", event);
    }

}
