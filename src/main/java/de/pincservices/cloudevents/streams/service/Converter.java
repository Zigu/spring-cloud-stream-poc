package de.pincservices.cloudevents.streams.service;

import java.io.IOException;
import java.net.URI;

import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pincservices.cloudevents.streams.dto.SampleDTO;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Converter {

    private final ObjectMapper objectMapper;

    public CloudEvent convertEvent(CloudEvent oldEvent, URI source) {
        return convert(oldEvent, source, " (processed)");
    }

    public CloudEvent convertStreamEvent(CloudEvent oldEvent, URI source) {
        return convert(oldEvent, source, " (processed in stream)");
    }

    private CloudEvent convert(CloudEvent oldEvent, URI source, String suffix) {
        CloudEvent newEvent;

        if (oldEvent.getData() != null && "SampleDTO_v1".equals(oldEvent.getType())) {
            try {
                SampleDTO sampleDTO = objectMapper.readValue(oldEvent.getData().toBytes(), SampleDTO.class);
                SampleDTO newDTO = new SampleDTO(sampleDTO.text() + suffix, sampleDTO.timestamp());
                CloudEventData newData = PojoCloudEventData.wrap(newDTO, objectMapper::writeValueAsBytes);

                newEvent = CloudEventBuilder.v1()
                        .withData(newData)
                        .withDataContentType(MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .withId(oldEvent.getId())
                        .withSource(source)
                        .withType("SampleDTO_v1")
                        .build();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            newEvent = CloudEventBuilder.v1()
                    .withData(oldEvent.getData())
                    .withDataContentType(oldEvent.getDataContentType())
                    .withId(oldEvent.getId())
                    .withSource(source)
                    .withType(oldEvent.getType())
                    .build();
        }
        return newEvent;
    }

}
