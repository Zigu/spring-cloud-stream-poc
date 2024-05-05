![build workflow](https://github.com/Zigu/spring-cloud-stream-poc/actions/workflows/gradle.yml/badge.svg)

# POC project for Spring Cloud Stream with Kafka

This project contains test classes to get familiar with
Spring Cloud Stream and Kafka Streams. 

The source code and the configuration contains comments
that shall help to understand pitfalls and what is needed
for which behaviour.

## use-native-encoding and use-native-decoding

The configuration `use-native-encoding` and `use-native-decoding`
in the application.yml controls two different sections.

For KStreams the part `spring.cloud.stream.kafka.streams.bindings` becomes
relevant. Because it needs the Serdes. The normal serializer/deserializer configurations at 
`spring.kafka.consumer`/`spring.kafka.producer` was completely ignored.

On the other hand for normal Messages, the `spring.kafka` configuration is
used when `use-native-encoding`/`use-native-decoding` is enabled.

Please be careful, when not activating this flag with Spring messaging and CloudEvents.
The `MessageConverter` will unwrap the payload and create a `Message<byte[]>` which results
in exceptions.

## `spring.cloud.function.definition`

The function definition is needed and but does not represent the order of the functions.


## Test libraries

I copied the libraries I used from several examples. But have not checked at the beginning
what they do. So I want to give some advises.

- spring-cloud-stream-test-binder: Is good to mock the bindings to kafka, if you don't want 
to start an actual kafka. If you want to combine KafkaListener in your tests and streams in 
your production code, don't activate this library.
- spring-kafka-test: I decided to use a Kafka testcontainer. If you prefer the embedded kafka 
(i.e. has much more convenience features) feel free to use this library, but don't forget to disable the testcontainer libraries. 
- kafka-streams-test-utils: If you do not want to use KafkaListener or KafkaTemplate, use this library to create Streams with the 
TestTopologyDriver.

## Local running via IDE

To let the application run locally, start a kafka via terminal command

```
docker-compose up -d
```

This also starts an akhq instance, available at http://localhost:7080 to view the messages and consumer groups.

Then start the application via run configuration.
