package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class StatsClientImpl implements StatsClient {
    private final RestClient restClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(@Value("${stats-server.url:http://localhost:9090}") String clientUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(clientUrl)
                .build();
    }

    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();
        log.info("сохранили информацию что был запрос");
    }

    @Override
    public Collection<ViewStats> getStat(String start, String end, List<String> urls, Boolean unique) {
        if (start == null || end == null) {
            log.warn("диапазон не может содержать null");
            throw new IllegalArgumentException("диапазон не может содержать null");
        }
        LocalDateTime startDataTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDataTime = LocalDateTime.parse(end, formatter);
        if (startDataTime.isAfter(endDataTime)) {
            log.warn("задан не верный диапазон");
            throw new IllegalArgumentException("задан не верный диапазон");
        }
        Collection<ViewStats> stats = restClient.get()
                .uri(uriBuilder -> uriGetStats(uriBuilder, start, end, urls, unique))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        log.info("запрос с параметрами");
        return stats;
    }

    private URI uriGetStats(UriBuilder uriBuilder, String start, String end, List<String> uris, Boolean unique) {
        UriBuilder builder = uriBuilder.path("/stats")
                .queryParam("start", start)
                .queryParam("end", end);
        if (uris != null && !uris.isEmpty()) {
            uris.forEach(url -> builder.queryParam("uris", url));
        }
        if (unique != null) {
            builder.queryParam("unique", unique);
        }
        log.info("конвертировали url + параметры");
        return builder.build();
    }
}

