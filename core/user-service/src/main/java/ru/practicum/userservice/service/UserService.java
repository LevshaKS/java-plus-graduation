package ru.practicum.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interactionapi.exception.ConflictException;
import ru.practicum.interactionapi.exception.NotFoundException;
import ru.practicum.userservice.mapper.UserMapper;
import ru.practicum.userservice.model.User;
import ru.practicum.interactionapi.dto.user.NewUserRequest;
import ru.practicum.interactionapi.dto.user.UserDto;
import ru.practicum.userservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Transactional
    public UserDto createUser(NewUserRequest request) {
        log.info("Создание пользователя с email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        User user = mapper.toUser(request);

        try {
            User savedUser = userRepository.save(user);
            log.info("Пользователь создан с ID: {}", savedUser.getId());
            return mapper.toUserDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с email " + request.getEmail() + " уже существует");
        }
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        return userRepository.findAllByIds(ids, pageable)
                .stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }



    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} удален", userId);
    }


    public UserDto findById(Long userId) {
        return mapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

}
