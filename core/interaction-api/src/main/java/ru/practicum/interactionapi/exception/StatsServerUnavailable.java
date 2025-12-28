package ru.practicum.interactionapi.exception;

public class StatsServerUnavailable extends RuntimeException{
    public StatsServerUnavailable (String message, Exception e) {
        super(message, e);
    }
}
