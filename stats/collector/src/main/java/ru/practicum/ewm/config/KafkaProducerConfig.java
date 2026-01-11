package ru.practicum.ewm.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    private final KafkaPropertiesCollectorConfig config;

    public KafkaProducerConfig(KafkaPropertiesCollectorConfig config) {
        this.config = config;
    }


    @Bean
    public KafkaProducer<Long, UserActionAvro> getProducer() {
        Properties properties = new Properties();
        properties.putAll(config.getProducers().getProperties());

        return new KafkaProducer<>(properties);
    }


}
