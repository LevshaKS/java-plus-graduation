package ru.practicum.eventservice.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.eventservice.category.service.CategoryService;
import ru.practicum.interactionapi.dto.LocationDto;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.eventservice.event.dto.EventShortDto;
import ru.practicum.eventservice.event.dto.NewEventDto;
import ru.practicum.eventservice.event.dto.UpdateEventUserRequest;
import ru.practicum.eventservice.event.model.Event;
import ru.practicum.eventservice.mapper.EventMapper;

import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;

import ru.practicum.interactionapi.dto.user.UserDto;
import ru.practicum.interactionapi.enums.EventState;
import ru.practicum.eventservice.event.repository.EventRepository;
import ru.practicum.interactionapi.exception.ConflictException;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.interactionapi.exception.ValidationException;
import ru.practicum.interactionapi.feignClient.CommentFeignClient;
import ru.practicum.interactionapi.feignClient.LocationFeignClient;
import ru.practicum.interactionapi.feignClient.RequestFeignClient;
import ru.practicum.interactionapi.feignClient.UserFeignClient;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventPrivateService {

    private final EventRepository eventRepository;

    private final CategoryService categoryService;
    private final EventMapper mapper;
    private final UserFeignClient userFeignClient;
    private final LocationFeignClient locationFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final CommentFeignClient commentFeignClient;


    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        log.info("получение информа о запросе на участие в событии текущего пользователя userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));


        UserDto initiator = userFeignClient.findById(userId);
        if (initiator == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            log.error("userId отличается от id создателя события");
            throw new ValidationException("Событие должно быть создано текущим пользователем");
        }
        return requestFeignClient.getEventParticipants(userId, eventId);
    }

    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        log.info("Изменение статуса заявок userId={}, eventId={}, requestIds={}", userId, eventId, dto.getRequestIds());
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));


        UserDto initiator = userFeignClient.findById(userId);
        if (initiator == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            log.error("userId отличается от id создателя события");
            throw new ValidationException("Событие должно быть создано текущим пользователем");
        }
        EventRequestStatusUpdateResult result = requestFeignClient.changeRequestStatus(userId, eventId, dto);
        System.out.println(result);

        return new EventRequestStatusUpdateResult();
    }


    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        log.info("Создание события пользователем userId={}", userId);

        if (dto.getEventDate() == null) {
            throw new ValidationException("Дата события не может быть пустой");
        }

        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата начала события должна быть минимум через 2 часа от текущего момента");
        }

        UserDto initiator = userFeignClient.findById(userId);
        if (initiator == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        //     .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        LocationDto locationDto = locationFeignClient.saveLocation(dto.getLocation());
        Event event = mapper.toEvent(dto,
                categoryService.getCategoryById(dto.getCategory()),
                locationDto,
                initiator);

        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event saved = eventRepository.save(event);

        Long confirmedRequests = requestFeignClient.countConfirmedRequestsByEventId(saved.getId());

        return mapper.toEventFullDto(saved, confirmedRequests, 0L, 0L, locationDto); // views = 0, comments = 0 для нового события
    }

    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        log.info("Обновление события пользователем userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Пользователь не может изменять чужое событие");
        }
        if (!(event.getState() == EventState.PENDING || event.getState() == EventState.CANCELED)) {
            throw new ConflictException("Изменять можно только события в состоянии ожидания или отмененные");
        }
        if (dto.getEventDate() != null &&
                dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть минимум через 2 часа от текущего момента");
        }

        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getCategory() != null) event.setCategory(categoryService.getCategoryById(dto.getCategory()));
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) event.setLocation(locationFeignClient.saveLocation(dto.getLocation()).getId());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);

        Long confirmedRequests = requestFeignClient.countConfirmedRequestsByEventId(updated.getId());
        Long commentsCount = commentFeignClient.countByEventId(updated.getId());
        LocationDto locationDto = locationFeignClient.getLocation(updated.getLocation());
        return mapper.toEventFullDto(updated, confirmedRequests, 0L, commentsCount, locationDto); // views 0
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        log.info("Получение событий пользователя userId={}", userId);

        if (userFeignClient.findById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
                .getContent();

        return convertToEventShortDtoList(events);
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события пользователем userId={}, eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Пользователь не может просматривать чужое событие");
        }

        Long confirmedRequests = requestFeignClient.countConfirmedRequestsByEventId(eventId);
        Long commentsCount = commentFeignClient.countByEventId(eventId);
        LocationDto locationDto = locationFeignClient.getLocation(event.getLocation());
        return mapper.toEventFullDto(event, confirmedRequests, 0L, commentsCount, locationDto); // views 0
    }

    // вспомогательный метод для конвертации списка
    private List<EventShortDto> convertToEventShortDtoList(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> commentsCountMap = getCommentsCountMap(eventIds);

        return events.stream()
                .map(event -> mapper.toEventShortDto(event,
                        confirmedRequestsMap.getOrDefault(event.getId(), 0L),
                        0L, // views пока 0
                        commentsCountMap.getOrDefault(event.getId(), 0L))) // добавляем количество комментариев
                .collect(Collectors.toList());
    }

    // метод для получения подтвержденных запросов
    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        List<Object[]> results = requestFeignClient.countConfirmedRequestsByEventIds(eventIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long.parseLong(result[0].toString())),
                        result -> (Long.parseLong(result[1].toString()))
                ));

    }

    // метод для получения количества комментариев
    private Map<Long, Long> getCommentsCountMap(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }

        return commentFeignClient.countCommentsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));

    }
}
