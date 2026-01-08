package ru.practicum.ewm.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ActionWeight {
    @Value("${application.action-weight.view}")
    private float viewMark;
    @Value("${application.action-weight.register}")
    private float registerMark;
    @Value("${application.action-weight.like}")
    private float likeMark;
}
