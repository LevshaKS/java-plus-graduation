package ru.practicum.eventservice.event.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.practicum.eventservice.event.service.EventPublicService;
import ru.practicum.interactionapi.dto.event.EventFullDto;


@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InternalEventController {

    private final EventPublicService eventPublicService;

    @GetMapping("/{eventId}")
    public EventFullDto eventById(@PathVariable Long eventId) {
        log.info("GET /events/{} - получение события по ID", eventId);


        return eventPublicService.getEventByIdTheRequest(eventId, "request.getRemoteAddr()", "request.getRequestURI()");
    }

}


