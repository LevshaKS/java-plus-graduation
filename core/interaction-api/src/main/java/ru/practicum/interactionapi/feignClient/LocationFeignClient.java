package ru.practicum.interactionapi.feignClient;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.interactionapi.dto.LocationDto;

import java.util.List;

@FeignClient(name = "location-service", path = "/internal/location")
public interface LocationFeignClient {

    @PostMapping
    LocationDto saveLocation(@Valid @RequestBody LocationDto locationDto) throws FeignException;

    @GetMapping("/{locationId}")
    LocationDto getLocation(@PathVariable Long locationId) throws FeignException;


    @GetMapping("/locations/{locationIds}")
    List<LocationDto> getLocationIds(@PathVariable List<Long> locationIds) throws FeignException;
}
