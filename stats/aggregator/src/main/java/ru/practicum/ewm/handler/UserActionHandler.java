package ru.practicum.ewm.handler;

import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface UserActionHandler {
    List<EventSimilarityAvro> expectSimilarity(UserActionAvro action);
}
