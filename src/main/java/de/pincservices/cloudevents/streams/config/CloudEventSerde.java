package de.pincservices.cloudevents.streams.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.cloudevents.kafka.CloudEventSerializer;

public class CloudEventSerde implements Serde<CloudEvent> {

    @Override
    public Serializer<CloudEvent> serializer() {
        return new CloudEventSerializer();
    }

    @Override
    public Deserializer<CloudEvent> deserializer() {
        return new CloudEventDeserializer();
    }
}
