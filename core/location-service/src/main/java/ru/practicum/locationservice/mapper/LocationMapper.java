package ru.practicum.locationservice.mapper;

import org.springframework.stereotype.Component;

import ru.practicum.interactionapi.dto.LocationDto;
import ru.practicum.locationservice.model.Location;

@Component
public class LocationMapper {
    // Маппинг локаций
    public Location toLocation(LocationDto dto) {
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getId(), location.getLat(), location.getLon());
    }
}
