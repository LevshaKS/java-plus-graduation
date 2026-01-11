package ru.practicum.ewm.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.repository.EventSimilarityRepository;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSimilarityHandler {

    private final EventSimilarityRepository eventSimilarityRepository;


    @Transactional

    public void handle(EventSimilarityAvro eventSimilarity) {
        Long eventA = eventSimilarity.getEventA();
        Long eventB = eventSimilarity.getEventB();

        if (!eventSimilarityRepository.existsByEventAAndEventB(eventA, eventB)) {
            eventSimilarityRepository.save(toEventSimilarity(eventSimilarity));
            log.info("Сохранение event_similarity {}", eventSimilarity);
        } else {
            EventSimilarity oldEventSimilarity = eventSimilarityRepository.findByEventAAndEventB(eventA, eventB);
            oldEventSimilarity.setScore(eventSimilarity.getScore());
            oldEventSimilarity.setTimestamp(eventSimilarity.getTimestamp());
        }
    }

    private EventSimilarity toEventSimilarity(EventSimilarityAvro similarityAvro) {
        return new EventSimilarity(null,
                similarityAvro.getEventA(),
                similarityAvro.getEventB(),
                similarityAvro.getScore(),
                similarityAvro.getTimestamp());

    }
}
