package ru.practicum.commentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.commentservice.mapper.CommentMapper;
import ru.practicum.commentservice.repository.CommentRepository;
import ru.practicum.commentservice.dto.CommentShortDto;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.interactionapi.enums.EventState;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.interactionapi.feignClient.EventFeignClient;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPublicService {

    private final CommentRepository commentRepository;

    private final CommentMapper mapper;
    private final EventFeignClient eventFeignClient;

    public List<CommentShortDto> getEventComments(Long eventId, int from, int size) {
        log.info("Получение комментариев для события eventId={}", eventId);

        // Проверяем, что событие существует и опубликовано
        EventFullDto event = eventFeignClient.eventById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable)
                .stream()
                .map(mapper::toCommentShortDto)
                .collect(Collectors.toList());
    }


    public Long getCountByEventId(Long eventId) {
        return commentRepository.countByEventId(eventId);
    }

    public List<Object[]> getCountCommentsByEventIds(@RequestBody List<Long> eventIds) {
        return commentRepository.countCommentsByEventIds(eventIds);
    }


}
