package ru.practicum.eventservice.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.eventservice.event.dto.EventShortDto;
import ru.practicum.eventservice.event.dto.NewEventDto;
import ru.practicum.eventservice.event.dto.UpdateEventUserRequest;
import ru.practicum.eventservice.event.service.EventPrivateService;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;


import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {

    private final EventPrivateService eventService;


    // ------------------ EVENTS ------------------

    @GetMapping("/events")
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /users/{}/events", userId);
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto dto) {
        log.info("POST /users/{}/events", userId);
        return eventService.createEvent(userId, dto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest dto) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.updateEvent(userId, eventId, dto);
    }

    // ------------------ PARTICIPATION REQUESTS ------------------

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return eventService.getRequestsForUserEvent(userId, eventId);

    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest dto) {
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);
        return eventService.changeRequestStatus(userId, eventId, dto);

    }

}