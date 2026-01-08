package ru.practicum.eventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.interactionapi.feignClient.*;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(clients = {ClientFeignController.class, UserFeignClient.class, CommentFeignClient.class, StatsFeignClient.class, LocationFeignClient.class, RequestFeignClient.class})
public class EventServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }


}
