package ru.practicum.commentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.commentservice.mapper.CommentMapper;
import ru.practicum.commentservice.repository.CommentRepository;
import ru.practicum.commentservice.dto.CommentDto;
import ru.practicum.commentservice.model.Comment;
import ru.practicum.interactionapi.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentAdminService {

    private final CommentRepository commentRepository;
    private final CommentMapper mapper;

    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Удаление комментария администратором commentId={}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с id=" + commentId + " не найден");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий с id={} удален администратором", commentId);
    }

    public List<CommentDto> getAllComments(int from, int size) {
        log.info("Получение всех комментариев администратором");

        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findAll(pageable)
                .stream()
                .map(mapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public CommentDto getComment(Long commentId) {
        log.info("Получение комментария администратором commentId={}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        return mapper.toCommentDto(comment);
    }
}
