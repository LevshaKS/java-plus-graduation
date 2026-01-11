package ru.practicum.requestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.interactionapi.feignClient.ClientFeignController;
import ru.practicum.interactionapi.feignClient.EventFeignClient;
import ru.practicum.interactionapi.feignClient.UserFeignClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(clients = {ClientFeignController.class, UserFeignClient.class, EventFeignClient.class})

public class RequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }


}
