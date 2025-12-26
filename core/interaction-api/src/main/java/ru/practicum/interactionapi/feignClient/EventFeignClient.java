package ru.practicum.interactionapi.feignClient;


import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.interactionapi.exception.GlobalExceptionHandler;
import ru.practicum.interactionapi.exception.StatsServerUnavailable;


@FeignClient(name = "event-service", path = "/internal/events")
public interface EventFeignClient {


    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "eventByIdFallback")
    @GetMapping("/{eventId}")
    EventFullDto eventById(@PathVariable Long eventId) throws FeignException;

    @GetMapping("/{eventId}")
    default EventFullDto eventByIdFallback(@PathVariable Long eventId, Throwable throwable) {
       return new EventFullDto();
    }
}
