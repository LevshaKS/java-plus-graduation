package ru.practicum.interactionapi.dto.event;

import lombok.Builder;
import lombok.Data;
import ru.practicum.interactionapi.dto.request.ParticipationRequestResponse;
import ru.practicum.interactionapi.enums.ExceptionStatus;

import java.util.List;
@Builder
@Data
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestResponse> confirmedRequests;
    private List<ParticipationRequestResponse> rejectedRequests;
        private ExceptionStatus exceptionStatus;
}