package ru.practicum.interactionapi.feignClient;


import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.practicum.interactionapi.dto.event.EventFullDto;


@FeignClient(name = "event-service", path = "/internal/events")
public interface EventFeignClient {

    @GetMapping("/{eventId}")
    EventFullDto eventById(@PathVariable Long eventId) throws FeignException;


}
