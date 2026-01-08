package ru.practicum.ewm.deserializer;


import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilarityAvroDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventSimilarityAvroDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
