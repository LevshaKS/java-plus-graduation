package ru.practicum.interactionapi.feignClient;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interactionapi.dto.user.NewUserRequest;
import ru.practicum.interactionapi.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserFeignClient {

    @PostMapping
    UserDto createUser(@Valid @RequestBody NewUserRequest request);

    @GetMapping
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                           @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                           @RequestParam(defaultValue = "10") @Positive int size) throws FeignException;

    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable Long userId) throws FeignException;

    @GetMapping("/{userId}")
    UserDto findById(@PathVariable Long userId) throws FeignException;

}
