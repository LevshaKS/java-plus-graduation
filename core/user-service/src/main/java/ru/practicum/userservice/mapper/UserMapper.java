package ru.practicum.userservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.interactionapi.dto.user.NewUserRequest;
import ru.practicum.interactionapi.dto.user.UserDto;
import ru.practicum.interactionapi.dto.user.UserShortDto;
import ru.practicum.userservice.model.User;

@Component
public class UserMapper {

    // Маппинг пользователей
    public User toUser(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}