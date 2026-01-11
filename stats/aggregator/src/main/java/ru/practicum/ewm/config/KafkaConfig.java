package ru.practicum.ewm.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration

public class KafkaConfig {


    private final KafkaPropertiesAggregatorConfig config;

    public KafkaConfig(KafkaPropertiesAggregatorConfig config) {
        this.config = config;
    }


    @Bean
    public KafkaProducer<Long, EventSimilarityAvro> getProducer() {
        Properties properties = new Properties();
        properties.putAll(config.getProducers().getProperties());

        return new KafkaProducer<>(properties);
    }


    @Bean
    public KafkaConsumer<Long, UserActionAvro> getConsumer() {
        Properties properties = new Properties();
        properties.putAll(config.getConsumers().getProperties());

        return new KafkaConsumer<>(properties);
    }
}