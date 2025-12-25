package ru.practicum.locationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.interactionapi.dto.LocationDto;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.locationservice.mapper.LocationMapper;
import ru.practicum.locationservice.model.Location;
import ru.practicum.locationservice.repository.LocationRepository;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper mapper;

    @Transactional
    public LocationDto getOrCreateLocation(LocationDto locationDto) {
        log.info("Получение или создание локации: lat={}, lon={}", locationDto.getLat(), locationDto.getLon());

        Optional<Location> existingLocation = locationRepository
                .findByLatAndLon(locationDto.getLat(), locationDto.getLon());

        if (existingLocation.isPresent()) {
            log.info("Локация уже существует с ID: {}", existingLocation.get().getId());
            return mapper.toLocationDto(existingLocation.get());
        }

        Location newLocation = mapper.toLocation(locationDto);
        Location savedLocation = locationRepository.save(newLocation);
        log.info("Создана новая локация с ID: {}", savedLocation.getId());

        return mapper.toLocationDto(savedLocation);
    }

    public LocationDto getLocation(Long locationId) {
        if (locationId == null) {
            log.info("нет такой локации с ID: {}", locationId);
            throw new NotFoundException("нет такой локации с ID: {}" + locationId);
        }
        return mapper.toLocationDto(locationRepository.getReferenceById(locationId));
    }

    public List<LocationDto> getLocationIds(List<Long> locationIds) {
        if (locationIds == null) {
            log.info("нет списка локации с ID: {}", locationIds);
            throw new NotFoundException("нет такой локации с ID: {}" + locationIds);
        }
        List<Location> locations = locationRepository.findAllById(locationIds);

        return locations.stream()
                .map(mapper::toLocationDto)
                .collect(Collectors.toList());


    }
}
