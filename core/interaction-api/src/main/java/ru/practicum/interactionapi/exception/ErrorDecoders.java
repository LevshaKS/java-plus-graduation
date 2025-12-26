package ru.practicum.interactionapi.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class ErrorDecoders implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 404) {
            throw new NotFoundException("При выполнении метода %s произошла ошибка Not Found".formatted(s));
        } else if (response.status() == 500) {
            throw new StatsServerUnavailable("При выполнении метода %s произошла ошибка StatsServerUnavailable".formatted(s) , new Exception());
        } else if (response.status() == 409) {
            throw new ConflictException("При выполнении метода %s произошла ошибка ConflictException".formatted(s));
        } else {
            return errorDecoder.decode(s, response);
        }
    }
}
