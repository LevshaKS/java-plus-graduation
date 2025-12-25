package ru.practicum.eventservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.interactionapi.dto.event.CategoryDto;
import ru.practicum.eventservice.category.dto.NewCategoryDto;
import ru.practicum.eventservice.category.model.Category;
import ru.practicum.eventservice.compilation.dto.CompilationDto;
import ru.practicum.eventservice.compilation.dto.NewCompilationDto;
import ru.practicum.eventservice.compilation.model.Compilation;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.eventservice.event.dto.EventShortDto;
import ru.practicum.eventservice.event.dto.NewEventDto;
import ru.practicum.eventservice.event.model.Event;
import ru.practicum.interactionapi.dto.LocationDto;
import ru.practicum.interactionapi.dto.user.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {
    // Маппинг событий
    public EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views, Long commentCount, LocationDto locationDto) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getInitiatorId(),
                locationDto,
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views,
                commentCount
        );
    }

    public EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views, Long commentCount) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getEventDate(),
                event.getInitiatorId(),
                event.getPaid(),
                event.getTitle(),
                views,
                event.getParticipantLimit(),
                commentCount
        );
    }

    // Маппинг подборок
    public Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> eventDtos = compilation.getEvents() != null ?
                compilation.getEvents().stream()
                        .map(event -> toEventShortDto(event, 0L, 0L, 0L))
                        .collect(Collectors.toList()) :
                List.of();

        return new CompilationDto(
                compilation.getId(),
                eventDtos,
                compilation.getPinned(),
                compilation.getTitle()
        );
    }

    public EventShortDto toEventShortDto(Event event) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                0L, // будет установлено в сервисе
                event.getEventDate(),
                event.getInitiatorId(),
                event.getPaid(),
                event.getTitle(),
                0L, // будет установлено в сервисе
                event.getParticipantLimit(),
                0L // будет установлено в сервисе
        );
    }

    public EventFullDto toEventFullDto(Event event, LocationDto locationDto) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                0L, // будет установлено в сервисе
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getInitiatorId(),
                locationDto,
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                0L, // будет установлено в сервисе (views)
                0L  // будет установлено в сервисе (commentsCount)
        );
    }

    // Маппинг новых событий
    public Event toEvent(NewEventDto dto, Category category, LocationDto location, UserDto initiator) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(location.getId())
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .title(dto.getTitle())
                .initiatorId(initiator.getId())
                .build();
    }


    // Маппинг категорий
    public Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }


}
