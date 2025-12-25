package ru.practicum.commentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 2000, message = "Текст комментария должен содержать от 1 до 2000 символов")
    @Column(name = "text", nullable = false, length = 2000)
    private String text;


    @JoinColumn(name = "event_id", nullable = false)
    private Long eventId;


    @JoinColumn(name = "author_id", nullable = false)
    private Long author;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}
