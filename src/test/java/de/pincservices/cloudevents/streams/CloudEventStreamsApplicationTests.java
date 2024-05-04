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
class CloudEventStreamsApplicationTests {

	@Container
	@ServiceConnection // This annotation only works for kafka template but not for spring cloud stream.
	static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
			.withExposedPorts(9092, 9093);

	@DynamicPropertySource
	static void kafkaProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
	}

	private static final List<CloudEvent> streamedCloudEvents = new CopyOnWriteArrayList<>();

	@Autowired
	private KafkaTemplate<String, CloudEvent> kafkaTemplate;

	@TestConfiguration
	static class Topics {

		@Bean
		public NewTopic streamIngestTopic() {
			return new NewTopic("internal.cloudevents.stream.ingest", 2, (short) 1);
		}

		@Bean
		public NewTopic transformedStreamIngestTopic() {
			return new NewTopic("internal.cloudevents.transformed.stream.ingest", 2, (short) 1);
		}
	}

	@Test
	void streamEvents() {
		CloudEvent event = CloudEventBuilder.v1()
				.withId(UUID.randomUUID().toString())
				.withSource(URI.create("http://localhost/junitTest"))
				.withType("text")
				.withData("plain/text", "TestText".getBytes(StandardCharsets.UTF_8))
				.build();

		kafkaTemplate.send("internal.cloudevents.stream.ingest", event.getId(), event);

		Awaitility.await().atMost(20, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(streamedCloudEvents).hasSize(1));

		assertThat(streamedCloudEvents.getFirst().getSource()).isEqualTo(URI.create("http://localhost/processEventStream"));
	}

	@KafkaListener(topics = "internal.cloudevents.transformed.stream.ingest", groupId = "junit")
	public void listenStreamedEvents(@Payload Message<CloudEvent> event) {
		streamedCloudEvents.add(event.getPayload());
	}

}
