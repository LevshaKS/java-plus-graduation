package ru.practicum.commentservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.commentservice.dto.CommentDto;
import ru.practicum.commentservice.dto.CommentShortDto;
import ru.practicum.commentservice.dto.NewCommentDto;
import ru.practicum.commentservice.model.Comment;
import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.interactionapi.dto.user.UserDto;

import java.time.LocalDateTime;

@Component
public class CommentMapper {
    // Маппинг комментариев

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor(),
                comment.getEventId(),
                comment.getCreatedOn(),
                comment.getUpdatedOn()
        );
    }

    public CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .build();
    }

    public Comment toComment(NewCommentDto newCommentDto, EventFullDto event, UserDto author) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .eventId(event.getId())
                .author(author.getId())
                .createdOn(LocalDateTime.now())
                .build();
    }

}

