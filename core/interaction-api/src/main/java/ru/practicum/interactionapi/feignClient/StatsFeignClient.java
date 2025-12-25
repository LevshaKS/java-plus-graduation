package ru.practicum.interactionapi.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interactionapi.dto.stats.EndpointHitDto;
import ru.practicum.interactionapi.dto.stats.ViewStats;


import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server")
public interface StatsFeignClient {

    @PostMapping("/hit")
    void saveHit(@Valid @RequestBody EndpointHitDto hitDto);//throws FeignException;


    @GetMapping("/stats")
    List<ViewStats> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    );


}