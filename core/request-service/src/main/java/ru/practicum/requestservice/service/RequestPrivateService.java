package ru.practicum.requestservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;

import ru.practicum.interactionapi.dto.event.EventFullDto;
import ru.practicum.interactionapi.dto.user.UserDto;
import ru.practicum.interactionapi.enums.EventState;
import ru.practicum.interactionapi.exception.ConflictException;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interactionapi.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interactionapi.dto.request.ParticipationRequestDto;
import ru.practicum.interactionapi.feignClient.EventFeignClient;
import ru.practicum.interactionapi.feignClient.UserFeignClient;
import ru.practicum.interactionapi.dto.request.ParticipationRequestResponse;
import ru.practicum.requestservice.mapper.RequestMapper;
import ru.practicum.requestservice.model.ParticipationRequest;
import ru.practicum.requestservice.model.RequestStatus;
import ru.practicum.requestservice.repository.ParticipationRequestRepository;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

@Slf4j
public class RequestPrivateService {

    private final ParticipationRequestRepository requestRepository;

    private final RequestMapper mapper;

    private final UserFeignClient userFeignClient;

    private final EventFeignClient eventFeignClient;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя userId={}", userId);

        return requestRepository.findAllByRequesterId(userId)
                .stream()
                .map(mapper::toParticipationRequestDto)
                .toList();
    }

    public List<Object[]> countConfirmedRequestsByEventIds(List<Long> eventIds) {
        log.info("Подсчет подтвержденных запросов для списка событий eventIds={}", eventIds);

        return requestRepository.countConfirmedRequestsByEventIds(eventIds);
    }

    public Long countConfirmedRequestsByEventId(Long eventId) {
        log.info("Подсчет подтвержденных запросов для события eventId={}", eventId);
        return requestRepository.countConfirmedRequestsByEventId(eventId);
    }

    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Создание запроса userId={}, eventId={}", userId, eventId);

        UserDto user = userFeignClient.findById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        //   .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        EventFullDto event = eventFeignClient.eventById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        //    .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getInitiator().equals(userId)) {
            throw new ConflictException("Нельзя добавить запрос на участие в своём событии");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос уже существует");
        }
        long confirmed = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() != 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников исчерпан");
        }

        RequestStatus status;
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .eventId(event.getId())
                .requesterId(user.getId())
                .created(LocalDateTime.now())
                .status(status)
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        return mapper.toParticipationRequestDto(saved);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса userId={}, requestId={}", userId, requestId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Пользователь не может отменить чужой запрос");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        return mapper.toParticipationRequestDto(saved);
    }

    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        log.info("Получение заявок для события userId={}, eventId={}", userId, eventId);

        EventFullDto event = eventFeignClient.eventById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        //       .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().equals(userId)) {
            throw new ConflictException("Пользователь не может просматривать чужие заявки");
        }

        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(mapper::toParticipationRequestDto)
                .toList();


    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        log.info("Изменение статуса заявок userId={}, eventId={}, requestIds={}", userId, eventId, dto.getRequestIds());

        EventFullDto event = eventFeignClient.eventById(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }  //   .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().equals(userId)) {
            throw new ConflictException("Пользователь не может изменять заявки чужого события");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.getRequestIds());

        List<ParticipationRequestResponse> confirmed = new ArrayList<>();
        List<ParticipationRequestResponse> rejected = new ArrayList<>();

        long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
        long limit = event.getParticipantLimit();


        List<ParticipationRequest> requestList = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус можно изменить только у заявок в ожидании");
            }

            if ("CONFIRMED".equalsIgnoreCase(dto.getStatus())) {
                if (limit != 0 && confirmedCount >= limit) {
                    throw new ConflictException("Достигнут лимит одобренных заявок");
                }
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                confirmed.add(mapper.toParticipationRequestResponse(request));
            } else if ("REJECTED".equalsIgnoreCase(dto.getStatus())) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(mapper.toParticipationRequestResponse(request));
            } else {
                throw new IllegalArgumentException("Неверный статус: " + dto.getStatus());
            }
            requestList.add(request);
        }


        // Если лимит достигнут, отклоняем все оставшиеся PENDING заявки
        if (limit != 0 && confirmedCount >= limit) {
            List<ParticipationRequest> pendingRequests = requestRepository.findAllByEventIdAndRequesterId(eventId, userId)
                    .stream()
                    .filter(r -> r.getStatus().equals(RequestStatus.PENDING))
                    .toList();

            for (ParticipationRequest pending : pendingRequests) {
                pending.setStatus(RequestStatus.REJECTED);
                rejected.add(mapper.toParticipationRequestResponse(pending));
                requestList.add(pending);
            }
        }
        requestRepository.saveAll(requestList);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }


}

