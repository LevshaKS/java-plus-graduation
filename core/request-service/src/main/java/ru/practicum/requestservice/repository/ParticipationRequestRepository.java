package ru.practicum.requestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.requestservice.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    // Запросы пользователя
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);


    // Запросы на событие пользователя
    @Query("SELECT pr FROM ParticipationRequest pr " +
            "WHERE pr.eventId = :eventId AND pr.requesterId = :requesterId")
    List<ParticipationRequest> findAllByEventIdAndRequesterId(
            @Param("eventId") Long eventId,
            @Param("requesterId") Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);


    // Проверка существования запроса
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    // Подсчет подтвержденных запросов для события
    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.eventId = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    // Подсчет подтвержденных запросов для списка событий
    @Query("SELECT pr.eventId, COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.eventId IN :eventIds AND pr.status = 'CONFIRMED' " +
            "GROUP BY pr.eventId")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);


}
