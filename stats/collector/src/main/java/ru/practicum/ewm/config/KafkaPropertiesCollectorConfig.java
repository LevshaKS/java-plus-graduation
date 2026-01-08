package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaPropertiesCollectorConfig {
    private Producers producers = new Producers();

    private String bootstrapServers;


    @Getter
    @Setter
    public class Producers {
        private Map<String, String> properties;
        private String topics;
    }
}