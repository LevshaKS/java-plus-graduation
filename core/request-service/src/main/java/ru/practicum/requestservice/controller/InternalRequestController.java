package ru.practicum.requestservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;
import ru.practicum.requestservice.service.RequestPrivateService;

import java.util.List;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
@Slf4j
public class InternalRequestController {
    private final RequestPrivateService requestService;

    @GetMapping("/{eventId}")
    public Long countConfirmedRequestsByEventId(@PathVariable Long eventId) {
        log.info("GET /{}", eventId);
        return requestService.countConfirmedRequestsByEventId(eventId);
    }

    @GetMapping("/request/{eventIds}")
    public List<Object[]> countConfirmedRequestsByEventIds(@PathVariable List<Long> eventIds) {
        log.info("GET {}", eventIds);
        return requestService.countConfirmedRequestsByEventIds(eventIds);
    }


    @GetMapping("/{userId}/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return requestService.getRequestsForUserEvent(userId, eventId);
    }

    @PutMapping("/{userId}/{eventId}/request")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest dto) {
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);
        return requestService.changeRequestStatus(userId, eventId, dto);
    }
}
