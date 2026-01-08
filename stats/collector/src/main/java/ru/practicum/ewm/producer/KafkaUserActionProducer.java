package ru.practicum.ewm.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserActionProducer implements AutoCloseable {

    private final Producer<Long, UserActionAvro> producer;


    public void send(UserActionAvro message, Long eventId, Instant timestamp, String topic) {
        ProducerRecord<Long, UserActionAvro> record = new ProducerRecord<>(topic, null,
                timestamp.toEpochMilli(), eventId, message);
        log.info("Отправляем {}", record);
        producer.send(record);
        producer.flush();
    }

    @Override
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(10));

    }
}
