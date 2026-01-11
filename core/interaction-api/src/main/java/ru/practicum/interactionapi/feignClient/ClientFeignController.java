package ru.practicum.interactionapi.feignClient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;

import java.util.stream.Stream;

@FeignClient(name = "client-controller", path = "/internal/clients")
public interface ClientFeignController {

    @GetMapping("/{userId}")
    Stream<RecommendedEventProto> getRecommendationsUser(@PathVariable Long userId,
                                                         @RequestParam(defaultValue = "10") @Positive int request) throws FeignException;


    @PutMapping("/{userId}/{eventId}/like")
    void addLike(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{userId}/{eventId}/view")
    void addView(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{userId}/{eventId}/register")
    void addRegister(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

}
