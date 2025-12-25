package ru.practicum.interactionapi.dto.event;

import lombok.Data;
import ru.practicum.interactionapi.dto.request.ParticipationRequestResponse;

import java.util.List;

@Data
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestResponse> confirmedRequests;
    private List<ParticipationRequestResponse> rejectedRequests;
}