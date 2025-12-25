package ru.practicum.commentservice;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.interactionapi.feignClient.EventFeignClient;
import ru.practicum.interactionapi.feignClient.UserFeignClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(clients = {UserFeignClient.class, EventFeignClient.class})
public class CommentServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}


