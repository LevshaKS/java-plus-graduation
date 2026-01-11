package ru.practicum.ewm.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.config.ActionWeight;

import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.repository.UserActionRepository;
import ru.yandex.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActionAnalyzerHandler {
    private final UserActionRepository userActionRepository;
    private final ActionWeight actionWeight;


    @Transactional

    public void handle(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Float newActionCalc = switch (action.getActionType()) {
            case LIKE -> actionWeight.getLikeMark();
            case REGISTER -> actionWeight.getRegisterMark();
            case VIEW -> actionWeight.getViewMark();
        };

        if (!userActionRepository.existsByEventIdAndUserId(eventId, userId)) {
            userActionRepository.save(toUserAction(action));
            log.info("Сохранили в БД действие {}", action);
        } else {
            UserAction userAction = userActionRepository.findByEventIdAndUserId(eventId, userId);
            if (userAction.getCalc() < newActionCalc) {
                userAction.setCalc(newActionCalc);
                userAction.setTimestamp(action.getTimestamp());
            }
        }
    }


    private UserAction toUserAction(UserActionAvro actionAvro) {
        return new UserAction(null,
                actionAvro.getEventId(),
                actionAvro.getUserId(),
                toCalc(actionAvro.getActionType()),
                actionAvro.getTimestamp());

    }

    private Float toCalc(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4f;
            case REGISTER -> 0.8f;
            case LIKE -> 1.0f;
        };
    }


}
