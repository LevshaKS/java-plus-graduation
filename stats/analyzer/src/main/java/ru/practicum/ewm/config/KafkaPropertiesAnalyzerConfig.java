package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaPropertiesAnalyzerConfig {
    private Consumers_u consumers_u = new Consumers_u();
    private Consumers_e consumers_e = new Consumers_e();
    private String bootstrapServers;
    private Duration consumerPollTimeout;

    @Getter
    @Setter
    public static class Consumers_u {
        private Map<String, String> properties;
        private List<String> topics;
    }

    @Getter
    @Setter
    public static class Consumers_e {
        private Map<String, String> properties;
        private List<String> topics;
    }
}
