package ru.practicum.interactionapi.feignClient;


import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;

import java.util.ArrayList;
import java.util.List;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestFeignClient {

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "countConfirmedRequestsByEventIdFallback")
    @GetMapping("/{eventId}")
    Long countConfirmedRequestsByEventId(@PathVariable Long eventId) throws FeignException;

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "countConfirmedRequestsByEventIdsFallback")
    @GetMapping("/request/{eventIds}")
    List<Object[]> countConfirmedRequestsByEventIds(@PathVariable List<Long> eventIds) throws FeignException;

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "getEventParticipantsFallback")
    @GetMapping("/{userId}/{eventId}/requests")
    List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId) throws FeignException;


    @PutMapping("/{userId}/{eventId}/request")
    EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest dto) throws FeignException;

    @GetMapping("/{eventId}")
   default   Long countConfirmedRequestsByEventIdFallback(@PathVariable Long eventId, Exception throwable) {
        return 0L;
    }



    @GetMapping("/request/{eventIds}")
    default List<Object[]> countConfirmedRequestsByEventIdsFallback(@PathVariable List<Long> eventIds, Exception throwable){
        return  new ArrayList<>();
        }


    @GetMapping("/{userId}/{eventId}/requests")
    default List<ParticipationRequestDto> getEventParticipantsFallback(
            @PathVariable Long userId,
            @PathVariable Long eventId, Exception throwable) {
        return  new ArrayList<>();
    }





}

