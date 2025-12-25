package ru.practicum.interactionapi.feignClient;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@FeignClient(name = "comment-service", path = "/internal/comment")
public interface CommentFeignClient {

    @GetMapping("/{eventId}")
    Long countByEventId(@PathVariable Long eventId) throws FeignException;

    @GetMapping("/comments/{eventIds}")
    List<Object[]> countCommentsByEventIds(@PathVariable List<Long> eventIds) throws FeignException;


}
