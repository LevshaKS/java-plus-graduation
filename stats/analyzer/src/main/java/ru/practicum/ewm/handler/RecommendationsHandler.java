package ru.practicum.ewm.handler;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.event.UserPredictionsRequestProto;
import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.repository.EventSimilarityRepository;
import ru.practicum.ewm.repository.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsHandler {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;


    public List<RecommendedEventProto> getRecommendationsUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int limit = request.getMaxResults();

        List<UserAction> userActions = userActionRepository.findAllByUserId(userId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp")));

        if (userActions.isEmpty()) {
            return List.of();
        }

        List<EventSimilarity> eventSimilarities = eventSimilarityRepository.findAllByEventAIn(userActions.stream()
                        .map(UserAction::getEventId)
                        .collect(Collectors.toSet()),
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score")));
        List<EventSimilarity> eventSimilaritiesB = eventSimilarityRepository.findAllByEventBIn(userActions.stream()
                        .map(UserAction::getEventId)
                        .collect(Collectors.toSet()),
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score")));

        List<Long> newEventIdsA = eventSimilarities.stream()
                .map(EventSimilarity::getEventB)
                .filter(eventId -> !userActionRepository.existsByEventIdAndUserId(eventId, userId))
                .distinct()
                .toList();

        List<Long> newEventIdsB = eventSimilaritiesB.stream()
                .map(EventSimilarity::getEventA)
                .filter(eventId -> !userActionRepository.existsByEventIdAndUserId(eventId, userId))
                .distinct()
                .toList();

        Set<Long> newEventIds = new HashSet<>(newEventIdsA);
        newEventIds.addAll(newEventIdsB);


        return newEventIds.stream()
                .map(eId -> RecommendedEventProto.newBuilder()
                        .setEventId(eId)
                        .setScore(calcScore(eId, userId, limit))
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(limit)
                .toList();
    }


    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        Long userId = request.getUserId();

        List<EventSimilarity> eventSimilaritiesA = eventSimilarityRepository.findAllByEventA(eventId,
                PageRequest.of(0, request.getMaxResults(), Sort.by(Sort.Direction.DESC, "score")));
        List<EventSimilarity> eventSimilaritiesB = eventSimilarityRepository.findAllByEventB(eventId,
                PageRequest.of(0, request.getMaxResults(), Sort.by(Sort.Direction.DESC, "score")));

        List<RecommendedEventProto> recommendationsA = new ArrayList<>(eventSimilaritiesA.stream()
                .filter(es -> !userActionRepository.existsByEventIdAndUserId(es.getEventB(), userId))
                .map(es -> RecommendedEventProto.newBuilder()
                        .setEventId(es.getEventB())
                        .setScore(es.getScore())
                        .build())
                .toList());

        List<RecommendedEventProto> recommendationsB = eventSimilaritiesB.stream()
                .filter(es -> !userActionRepository.existsByEventIdAndUserId(es.getEventA(), userId))
                .map(es -> RecommendedEventProto.newBuilder()
                        .setEventId(es.getEventA())
                        .setScore(es.getScore())
                        .build())
                .toList();

        recommendationsA.addAll(recommendationsB);

        return recommendationsA.stream()
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();
    }


    public List<RecommendedEventProto> getInteractionCount(InteractionsCountRequestProto request) {
        return new ArrayList<>(request.getEventIdList().stream()
                .map(eId -> RecommendedEventProto.newBuilder()
                        .setEventId(eId)
                        .setScore(userActionRepository.getSumWeightByEventId(eId))
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .toList());
    }

    private float calcScore(Long eventId, Long userId, int limit) {
        List<EventSimilarity> eventSimilaritiesA = eventSimilarityRepository.findAllByEventA(eventId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score")));

        List<EventSimilarity> eventSimilaritiesB = eventSimilarityRepository.findAllByEventB(eventId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score")));

        Map<Long, Double> viewedEventScores = eventSimilaritiesA.stream()
                .filter(es -> userActionRepository.existsByEventIdAndUserId(es.getEventB(), userId))
                .collect(Collectors.toMap(EventSimilarity::getEventB, EventSimilarity::getScore));

        Map<Long, Double> viewedEventScoresB = eventSimilaritiesB.stream()
                .filter(es -> userActionRepository.existsByEventIdAndUserId(es.getEventA(), userId))
                .collect(Collectors.toMap(EventSimilarity::getEventA, EventSimilarity::getScore));

        viewedEventScores.putAll(viewedEventScoresB);

        Map<Long, Float> actionCalc = userActionRepository.findAllByEventIdInAndUserId(viewedEventScores.keySet(),
                        userId).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getCalc));

        Float sumWeightedCalc = ((Double) viewedEventScores.entrySet().stream()
                .map(entry -> actionCalc.get(entry.getKey()) * entry.getValue())
                .mapToDouble(Double::doubleValue).sum())
                .floatValue();

        Float sumScores = ((Double) viewedEventScores.values().stream().mapToDouble(Double::doubleValue).sum())
                .floatValue();

        return sumWeightedCalc / sumScores;
    }

}
