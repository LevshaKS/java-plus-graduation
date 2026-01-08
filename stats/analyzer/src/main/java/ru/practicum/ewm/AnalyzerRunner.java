package ru.practicum.ewm;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.EventSimilarityProcessor;
import ru.practicum.ewm.service.UserActionProcessor;

@Component
@AllArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    private final EventSimilarityProcessor eventSimilarityProcessor;
    private final UserActionProcessor userActionProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread eventSimilarityThread = new Thread(eventSimilarityProcessor);
        eventSimilarityThread.setName("EventSimilarityThread");
        eventSimilarityThread.start();
        // запускаем в отдельном потоке обработчик событий
        // от пользовательских хабов

        // В текущем потоке начинаем обработку
        // снимков состояния датчиков
        userActionProcessor.run();
    }
}
