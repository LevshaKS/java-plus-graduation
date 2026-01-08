package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.config.KafkaPropertiesAnalyzerConfig;
import ru.practicum.ewm.handler.UserActionAnalyzerHandler;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;
import org.apache.kafka.common.errors.WakeupException;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {
    private final Consumer<Long, UserActionAvro> consumer;
    private final UserActionAnalyzerHandler handler;
    private final KafkaPropertiesAnalyzerConfig config;

    @Override
    public void run() {

        try {
            consumer.subscribe(config.getConsumers_u().getTopics());

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(config.getConsumerPollTimeout());

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    UserActionAvro action = record.value();
                    log.info("Полученеи действий {}", action);

                    handler.handle(action);
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка чтения данных из топика {}", config.getConsumers_u().getTopics());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

}
