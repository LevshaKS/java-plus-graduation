package ru.practicum.commentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.commentservice.dto.NewCommentDto;
import ru.practicum.commentservice.mapper.CommentMapper;
import ru.practicum.commentservice.repository.CommentRepository;
import ru.practicum.commentservice.dto.CommentDto;
import ru.practicum.commentservice.dto.UpdateCommentDto;
import ru.practicum.commentservice.model.Comment;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.interactionapi.dto.user.UserDto;
import ru.practicum.interactionapi.enums.EventState;
import ru.practicum.interactionapi.exception.ConflictException;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.interactionapi.feignClient.EventFeignClient;
import ru.practicum.interactionapi.feignClient.UserFeignClient;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentPrivateService {

    private final CommentRepository commentRepository;
    private final CommentMapper mapper;
    private final UserFeignClient userFeignClient;
    private final EventFeignClient eventFeignClient;


    private static final int EDIT_TIME_LIMIT_HOURS = 24; // Время для редактирования - 24 часа

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Создание комментария пользователем userId={}, eventId={}", userId, eventId);

        UserDto author = userFeignClient.findById(userId);
        if (author == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        EventFullDto event = eventFeignClient.eventById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        // Проверяем, что событие опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя комментировать неопубликованное событие");
        }

        Comment comment = mapper.toComment(dto, event, author);
        comment.setCreatedOn(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий создан с id={}", savedComment.getId());
        return mapper.toCommentDto(savedComment);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        log.info("Обновление комментария userId={}, commentId={}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        // Проверяем, что пользователь является автором комментария
        if (!comment.getAuthor().equals(userId)) {
            throw new ConflictException("Пользователь не может редактировать чужой комментарий");
        }

        // Проверяем время редактирования
        if (comment.getCreatedOn().isBefore(LocalDateTime.now().minusHours(EDIT_TIME_LIMIT_HOURS))) {
            throw new ConflictException("Время редактирования комментария истекло");
        }

        comment.setText(dto.getText());
        comment.setUpdatedOn(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        log.info("Комментарий с id={} обновлен", commentId);
        return mapper.toCommentDto(updatedComment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Удаление комментария userId={}, commentId={}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        // Проверяем, что пользователь является автором комментария
        if (!comment.getAuthor().equals(userId)) {
            throw new ConflictException("Пользователь не может удалить чужой комментарий");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий с id={} удален", commentId);
    }

    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        log.info("Получение комментариев пользователя userId={}", userId);

        // Проверяем, что пользователь существует
        if (userFeignClient.findById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }


        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findByAuthorIdOrderByCreatedOnDesc(userId, pageable)
                .stream()
                .map(mapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
