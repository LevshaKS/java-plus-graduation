package ru.practicum.interactionapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private Long id;

    @NotNull(message = "Широта не может быть пустой")
    private Float lat;

    @NotNull(message = "Долгота не может быть пустой")
    private Float lon;
}
