package ru.practicum.eventservice.event.model;

public enum StateAction {
    // Действия для админов
    PUBLISH_EVENT,
    REJECT_EVENT,

    // Действия для пользователей
    SEND_TO_REVIEW,
    CANCEL_REVIEW
}
