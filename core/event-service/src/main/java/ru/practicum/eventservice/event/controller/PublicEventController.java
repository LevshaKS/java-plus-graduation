package ru.practicum.eventservice.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.eventservice.event.dto.EventSearchParams;
import ru.practicum.eventservice.event.dto.EventShortDto;
import ru.practicum.eventservice.event.service.EventPublicService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {

    private final EventPublicService eventPublicService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            HttpServletRequest request) {

        log.info("GET /events - поиск событий с параметрами: text={}, categories={}, paid={}",
                text, categories, paid);

        EventSearchParams params = new EventSearchParams(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size
        );

        return eventPublicService.getEvents(params, request.getRemoteAddr(), request.getRequestURI());
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@RequestHeader(name = "X-EWM-USER-ID", required = false) long userId, @PathVariable Long id, HttpServletRequest request) {
        log.info("GET /events/{} - получение события по ID", id);

        return eventPublicService.getEventById(userId, id, request.getRemoteAddr(), request.getRequestURI());
    }


    @GetMapping("/recommendations")
    public List<EventShortDto> getEventRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                       @RequestParam(defaultValue = "10") int request) {
        return eventPublicService.getEventRecommendations(userId, request);
    }

    @PutMapping("/{eventId}/like")
    public void addLike(@RequestHeader("X-EWM-USER-ID") Long userId, @PathVariable Long eventId) {
        eventPublicService.addLike(userId, eventId);
    }
}
