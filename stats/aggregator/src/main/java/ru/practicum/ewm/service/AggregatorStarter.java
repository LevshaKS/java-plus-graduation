package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.config.KafkaPropertiesAggregatorConfig;
import ru.practicum.ewm.handler.UserActionHandler;
import ru.practicum.ewm.producer.KafkaEventSimilarityProducer;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;


import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorStarter {
    private final KafkaEventSimilarityProducer producer;
    private final Consumer<Long, UserActionAvro> consumer;
    private final UserActionHandler handler;
    private final KafkaPropertiesAggregatorConfig config;


    public void start() {
        try {
            consumer.subscribe(config.getConsumers().getTopics());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("сработал хук на завершение JVM. перерывается консьюмер");
                consumer.wakeup();
            }));
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(config.getConsumerPollTimeout());

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    UserActionAvro action = record.value();
                    log.info("обрабатываем действие пользователя {}", action);

                    List<EventSimilarityAvro> result = handler.expectSimilarity(action);
                    log.info("Получили список коэффициентов схожести {}", result);

                    producer.send(result, config.getProducers().getTopics());
                    producer.flush();
                }

                consumer.commitSync();
            }


        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий ", e);
        } finally {

            try {
                producer.flush();
                consumer.commitSync();

                // Перед тем, как закрыть продюсер и консьюмер, нужно убедится,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы


            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}

