package ru.practicum.locationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import ru.practicum.interactionapi.dto.LocationDto;

import ru.practicum.locationservice.service.LocationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/location")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public LocationDto saveLocation(@Valid @RequestBody LocationDto locationDto) {
        log.info("POST /admin/compilations - создание подборки: {}", locationDto);
        return locationService.getOrCreateLocation(locationDto);
    }

    @GetMapping("/{locationId}")
    public LocationDto getLocation(@PathVariable Long locationId) {
        log.info("GET  - поиск запроса локации: {}", locationId);
        return locationService.getLocation(locationId);
    }

    @GetMapping("/locations/{locationIds}")
    public List<LocationDto> getLocationIds(@PathVariable List<Long> locationIds) {
        log.info("GET  - поиск запроса списка локаций: {}", locationIds);
        return locationService.getLocationIds(locationIds);
    }
}