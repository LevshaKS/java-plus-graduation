package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.ewm.service.AggregatorStarter;

@EnableDiscoveryClient
@SpringBootApplication
public class AggregatorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApplication.class, args);

        AggregatorStarter aggregator = context.getBean(AggregatorStarter.class);
        aggregator.start();
    }
}
