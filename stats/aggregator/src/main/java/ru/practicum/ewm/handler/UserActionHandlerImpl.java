package ru.practicum.ewm.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.practicum.ewm.config.ActionWeight;
import ru.yandex.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {
    private final Map<Long, Map<Long, Double>> eventActions = new HashMap<>();
    private final Map<Long, Double> eventWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> weightsSum = new HashMap<>();
    private final ActionWeight actionWeight;


    @Override
    public List<EventSimilarityAvro> expectSimilarity(UserActionAvro action) {
        List<EventSimilarityAvro> similarity = new ArrayList<>();
        Long actionEventId = action.getEventId();
        Long actionUserId = action.getUserId();

        double delta = updateEventAction(action);

        if (delta > 0) {

            eventWeights.merge(actionEventId, delta, Double::sum);
            log.info("Новые weights события с id = {} равен {}", actionEventId, eventWeights.get(actionEventId));

            Set<Long> anotherEvents = eventActions.keySet().stream()
                    .filter(id -> !Objects.equals(id, actionEventId))
                    .collect(Collectors.toSet());

            log.info("получаем коэффициенты схожести для событий {}", anotherEvents);

            anotherEvents.forEach(ae -> {
                double sim = getMinWeightsSum(actionEventId, ae, delta, actionUserId) /
                        (Math.sqrt(eventWeights.get(actionEventId)) * Math.sqrt(eventWeights.get(ae)));

                if (sim > 0.0) {
                    similarity.add(EventSimilarityAvro.newBuilder()
                            .setEventA(Math.min(actionEventId, ae))
                            .setEventB(Math.max(actionEventId, ae))
                            .setScore(sim)
                            .setTimestamp(Instant.now())
                            .build());
                }
            });
        }

        return similarity;
    }

    private Double updateEventAction(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double newWeight = Double.valueOf(mapActionToWeight(action.getActionType()));

        Map<Long, Double> userActions = eventActions.computeIfAbsent(eventId, k -> new HashMap<>());
        log.info("Получили действия пользователей с событием {}: {}", eventId, userActions);
        Double oldWeight = userActions.get(userId);

        if (oldWeight == null || newWeight > oldWeight) {
            userActions.put(userId, newWeight);
            eventActions.put(eventId, userActions);
            double delta = oldWeight == null ? newWeight : newWeight - oldWeight;
            log.info("Возвращаем значение delta = {}", delta);
            return delta;
        }
        log.info("вес не изменился, возвращаем значение delta = 0.0");
        return 0.0;
    }

    private Double getMinWeightsSum(Long eventA, Long eventB, Double delta, Long userId) {
        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);

        Double weightA = eventActions.get(eventA).get(userId);
        Double weightB = eventActions.get(eventB).get(userId);

        if (weightB == null) {
            log.info("Пользователь {} не взаимодействовал с событием {} -> минимальный значение не изменилось", userId, eventB);
            return 0.0;
        }

        Map<Long, Double> innerMap = weightsSum.computeIfAbsent(first, k -> new HashMap<>());

        Double weight = innerMap.get(second);
        if (weight == null) {
            weight = calcMinWeightsSum(eventA, eventB);
            log.info("минимальная сумма не была посчитана, посчитали новую сумму = {}", weight);
            innerMap.put(eventB, weight);
            weightsSum.put(first, innerMap);
            return weight;
        }

        double newWeight;
        if (weightA > weightB && (weightA - delta) < weightB) {
            newWeight = weight + (weightB - (weightA - delta));
            log.info("произошла смена минимального значения, минимальный вес увеличился на {}", newWeight);
        } else if (weightA <= weightB) {
            log.info("после обновления weightA на delta он меньше или равен eventB и общий вес увеличился на delta");
            newWeight = weight + delta;
        } else {
            log.info("минимальное значение осталось не измененное");
            return weight;
        }
        weightsSum.get(first).put(second, newWeight);
        return newWeight;
    }

    private Double calcMinWeightsSum(Long eventA, Long eventB) {
        List<Double> weights = new ArrayList<>();

        Map<Long, Double> userActionsA = eventActions.get(eventA);
        Map<Long, Double> userActionsB = eventActions.get(eventB);

        userActionsA.forEach((aUser, aWeight) -> {
            if (userActionsB.containsKey(aUser)) {
                weights.add(Math.min(aWeight, userActionsB.get(aUser)));
            }
        });

        if (weights.isEmpty()) {
            return 0.0;
        }
        return weights.stream().mapToDouble(Double::doubleValue).sum();
    }

    private Float mapActionToWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> actionWeight.getViewMark();
            case REGISTER -> actionWeight.getRegisterMark();
            case LIKE -> actionWeight.getLikeMark();
        };
    }
}
