package ru.practicum.requestservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created", nullable = false)
    @NotNull(message = "Дата создания запроса не может быть пустой")
    private LocalDateTime created;


    @Column(name = "event_id", nullable = false)
    @NotNull(message = "Событие не может быть пустым")
    private Long eventId;


    @Column(name = "requester_id", nullable = false)
    @NotNull(message = "Пользователь не может быть пустым")
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
}
