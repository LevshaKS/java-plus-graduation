package ru.practicum.eventservice.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.eventservice.category.service.CategoryService;
import ru.practicum.interactionapi.dto.LocationDto;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.eventservice.event.dto.UpdateEventAdminRequest;
import ru.practicum.eventservice.event.model.Event;
import ru.practicum.interactionapi.enums.EventState;
import ru.practicum.eventservice.event.model.StateAction;
import ru.practicum.eventservice.mapper.EventMapper;

import ru.practicum.eventservice.event.repository.EventRepository;
import ru.practicum.interactionapi.exception.ConflictException;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.interactionapi.exception.ValidationException;
import ru.practicum.interactionapi.feignClient.CommentFeignClient;
import ru.practicum.interactionapi.feignClient.LocationFeignClient;
import ru.practicum.interactionapi.feignClient.RequestFeignClient;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventAdminService {

    private final EventRepository eventRepository;

    private final CategoryService categoryService;

    private final EventMapper mapper;
    private final RequestFeignClient requestFeignClient;
    private final LocationFeignClient locationFeignClient;
    private final CommentFeignClient commentFeignClient;

    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states,
                                        List<Long> categories, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, int from, int size) {
        log.info("Получение событий для админа: users={}, states={}, categories={}", users, states, categories);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findEventsForAdmin(
                users, states, categories, rangeStart, rangeEnd, pageable
        ).getContent();

        return convertToEventFullDtoList(events);
    }

    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        log.info("Обновление события админом: eventId={}", eventId);

        Event event = getEventById(eventId);

        // Валидация времени события
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(request.getEventDate());
        }

        // Обновление полей
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationFeignClient.saveLocation(request.getLocation()).getId());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        // Обработка изменения состояния
        if (request.getStateAction() != null) {
            handleStateAction(event, request.getStateAction());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с ID {} обновлено админом", eventId);

        return convertToEventFullDto(updatedEvent);
    }

    private void handleStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT:
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Событие можно публиковать только если оно в состоянии ожидания модерации");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Дата начала события должна быть не ранее чем за час от даты публикации");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Событие можно отклонить только если оно еще не опубликовано");
                }
                event.setState(EventState.CANCELED);
                break;
        }
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
    }

    private EventFullDto convertToEventFullDto(Event event) {
        Long confirmedRequests = requestFeignClient.countConfirmedRequestsByEventId(event.getId());
        Long commentsCount = commentFeignClient.countByEventId(event.getId());
        LocationDto locationDto = locationFeignClient.getLocation(event.getLocation());
        return mapper.toEventFullDto(event, confirmedRequests, 0L, commentsCount, locationDto);
    }

    private List<EventFullDto> convertToEventFullDtoList(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> commentsCountMap = getCommentsCountMap(eventIds);


        List<Long> locationIds = events.stream()
                .map(Event::getLocation)
                .collect(Collectors.toList());

        Map<Long, LocationDto> locationDtoMap = getLocationDtoMap(locationIds);


        return events.stream()
                .map(event -> mapper.toEventFullDto(
                        event,
                        confirmedRequestsMap.getOrDefault(event.getId(), 0L),
                        0L, // views пока 0
                        commentsCountMap.getOrDefault(event.getId(), 0L),
                        locationDtoMap.get(event.getLocation())
                ))
                .collect(Collectors.toList());
    }

    private Map<Long, LocationDto> getLocationDtoMap(List<Long> locationIds) {
        List<LocationDto> locationDtoMap = locationFeignClient.getLocationIds(locationIds);
        return locationDtoMap.stream()
                .collect(Collectors.toMap(locationDto -> locationDto.getId(), locationDto -> locationDto
                ));
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        List<Object[]> results = requestFeignClient.countConfirmedRequestsByEventIds(eventIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long.parseLong(result[0].toString())),
                        result -> (Long.parseLong(result[1].toString()))
                ));
    }

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
