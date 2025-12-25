package ru.practicum.interactionapi.feignClient;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestFeignClient {


    @GetMapping("/{eventId}")
    Long countConfirmedRequestsByEventId(@PathVariable Long eventId);// throws FeignException;


    @GetMapping("/request/{eventIds}")
    List<Object[]> countConfirmedRequestsByEventIds(@PathVariable List<Long> eventIds);// throws FeignException;


    @GetMapping("/{userId}/{eventId}/requests")
    List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId);//throws FeignException;


    @PutMapping("/{userId}/{eventId}/request")
    EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest dto);//throws FeignException;


}




