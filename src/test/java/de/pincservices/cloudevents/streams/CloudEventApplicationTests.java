package de.pincservices.cloudevents.streams;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class CloudEventApplicationTests {

    @Container
    @ServiceConnection // This annotation only works for kafka template but not for spring cloud stream.
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .withExposedPorts(9092, 9093);

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
    }

    private static final List<CloudEvent> cloudEvents = new CopyOnWriteArrayList<>();

    @Autowired
    private KafkaTemplate<String, CloudEvent> kafkaTemplate;

    @TestConfiguration
    static class Topics {

        @Bean
        public NewTopic ingestTopic() {
            return new NewTopic("internal.cloudevents.ingest", 2, (short) 1);
        }

        @Bean
        public NewTopic transformedIngestTopic() {
            return new NewTopic("internal.cloudevents.transformed.ingest", 2, (short) 1);
        }

    }

    @Test
    void events() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("http://localhost/junitTest"))
                .withType("text")
                .withData("plain/text", "TestText".getBytes(StandardCharsets.UTF_8))
                .build();

        kafkaTemplate.send("internal.cloudevents.ingest", event.getId(), event);

        Awaitility.await().atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(cloudEvents).hasSize(1));

        assertThat(cloudEvents.getFirst().getSource()).isEqualTo(URI.create("http://localhost/processEvent"));
    }

    @KafkaListener(topics = "internal.cloudevents.transformed.ingest", groupId = "junit")
    public void listenEvents(@Payload Message<CloudEvent> event) {
        cloudEvents.add(event.getPayload());
    }
}
