package ru.practicum.commentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.commentservice.service.CommentPublicService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comment")
public class InternalCommentController {

    private final CommentPublicService commentPublicService;

    @GetMapping("/{eventId}")
    Long countByEventId(@PathVariable Long eventId) {
        return commentPublicService.getCountByEventId(eventId);
    }


    @GetMapping("/comments/{eventIds}")
    List<Object[]> countCommentsByEventIds(@PathVariable List<Long> eventIds) {
        return commentPublicService.getCountCommentsByEventIds(eventIds);
    }

}
