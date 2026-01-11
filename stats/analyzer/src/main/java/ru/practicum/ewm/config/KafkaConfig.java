package ru.practicum.ewm.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConfig {
    private final KafkaPropertiesAnalyzerConfig config;


    public KafkaConfig(KafkaPropertiesAnalyzerConfig config) {
        this.config = config;
    }

    @Bean
    public KafkaConsumer<Long, UserActionAvro> getConsumerUser() {
        Properties properties = new Properties();
        properties.putAll(config.getConsumers_u().getProperties());

        return new KafkaConsumer<>(properties);
    }

    @Bean
    public KafkaConsumer<Long, EventSimilarityAvro> getConsumerEvent() {
        Properties properties = new Properties();
        properties.putAll(config.getConsumers_e().getProperties());

        return new KafkaConsumer<>(properties);
    }
}
