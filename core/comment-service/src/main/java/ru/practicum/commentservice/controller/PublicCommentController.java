package ru.practicum.commentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.commentservice.dto.CommentShortDto;
import ru.practicum.commentservice.service.CommentPublicService;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCommentController {

    private final CommentPublicService commentPublicService;

    @GetMapping
    public List<CommentShortDto> getEventComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("Запрос комментариев для события eventId={}, from={}, size={}", eventId, from, size);
        return commentPublicService.getEventComments(eventId, from, size);
    }
}
