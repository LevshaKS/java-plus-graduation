package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.config.KafkaPropertiesAnalyzerConfig;
import ru.practicum.ewm.handler.EventSimilarityHandler;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {
    private final Consumer<Long, EventSimilarityAvro> consumer;
    private final EventSimilarityHandler handler;
    private final KafkaPropertiesAnalyzerConfig config;

    @Override
    public void run() {

        try {
            consumer.subscribe(config.getConsumers_e().getTopics());
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(config.getConsumerPollTimeout());

                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    EventSimilarityAvro eventSimilarity = record.value();
                    log.info("Получение коэффициента схожести: {}", eventSimilarity);

                    handler.handle(eventSimilarity);
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка чтения данных из топика {}", config.getConsumers_e().getTopics());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

}
