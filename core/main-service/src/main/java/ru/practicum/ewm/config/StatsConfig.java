package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.StatsClient;
import ru.practicum.client.StatsClientImpl;
import ru.practicum.ewm.exception.StatsServerUnavailable;

@Configuration
public class StatsConfig {

//    @Value("${stats-server.url}")
//    private String statsServerUrl;


    @Value("${app.name}")
    private String appName;


    @Bean
    public StatsClient statsClient() {
        ServiceInstance instance = getInstance();
        String statsServerUrl = ("http://" + instance.getHost() + ":" + instance.getPort());
        return new StatsClientImpl(statsServerUrl);
    }


    @Autowired
    private DiscoveryClient discoveryClient;


    private ServiceInstance getInstance() {
        try {

            return discoveryClient
                    .getInstances(appName)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + appName,
                    exception
            );
        }
    }


}


