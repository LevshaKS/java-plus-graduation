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

        return recommendationsClient.getRecommendationsUser(userId, request);
    }

    @PutMapping("/{userId}/{eventId}/like")
    public void addLike(@PathVariable Long userId, @PathVariable Long eventId) {
        collectorClient.collectUserAction(eventId, userId, ActionTypeProto.ACTION_LIKE, Instant.now());
        ;
    }
}
