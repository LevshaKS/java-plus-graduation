package ru.practicum.requestservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;
import ru.practicum.interactionapi.dto.request.ParticipationRequestResponse;
import ru.practicum.requestservice.model.ParticipationRequest;

@Component
public class RequestMapper {
    // Маппинг запросов на участие
    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .created(request.getCreated())
                .status(request.getStatus().name())
                .build();
    }

    public ParticipationRequestResponse toParticipationRequestResponse(ParticipationRequest request) {
        if (request == null) return null;

        ParticipationRequestResponse response = new ParticipationRequestResponse();
        response.setId(request.getId());
        response.setEvent(request.getEventId());
        response.setRequester(request.getRequesterId());
        response.setCreated(request.getCreated());
        response.setStatus(request.getStatus().name());
        return response;
    }

}
