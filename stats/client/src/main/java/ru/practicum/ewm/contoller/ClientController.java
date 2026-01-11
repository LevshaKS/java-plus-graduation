package ru.practicum.ewm.contoller;


import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.CollectorClient;
import ru.practicum.ewm.RecommendationsClient;
import ru.practicum.ewm.grpc.stats.event.ActionTypeProto;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;

import java.time.Instant;
import java.util.stream.Stream;

@RestController
@RequestMapping("/internal/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {
    private final CollectorClient collectorClient;
    private final RecommendationsClient recommendationsClient;


    @GetMapping("/{userId}")
    Stream<RecommendedEventProto> getRecommendationsUser(@PathVariable Long userId,
                                                         @RequestParam(defaultValue = "10") @Positive int request) {
        log.info("GET /{}", userId);
        return recommendationsClient.getRecommendationsUser(userId, request);
    }

    @PutMapping("/{userId}/{eventId}/like")
    void addLike(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("PUT /{}/{}/like", userId, eventId);
        collectorClient.collectUserAction(eventId, userId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }

    @GetMapping("/{userId}/{eventId}/view")
    void addView(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET /{}/{}/view", userId, eventId);
        collectorClient.collectUserAction(eventId, userId, ActionTypeProto.ACTION_VIEW, Instant.now());
    }

    @GetMapping("/{userId}/{eventId}/register")
    void addRegister(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET /{}/{}/register", userId, eventId);
        collectorClient.collectUserAction(eventId, userId, ActionTypeProto.ACTION_REGISTER, Instant.now());

    }

}
