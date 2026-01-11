package ru.practicum.ewm.producer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaEventSimilarityProducer implements AutoCloseable {

    private final Producer<Long, EventSimilarityAvro> producer;


    public void send(List<EventSimilarityAvro> messages, String topic) {

        messages.stream()
                .map(message -> new ProducerRecord<>(topic, null,
                        message.getTimestamp().toEpochMilli(), message.getEventA(), message))
                .forEach(producer::send);

    }


    public void flush() {
        producer.flush();
    }

    @Override
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(10));

    }
}
