package ru.practicum.eventservice.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.eventservice.category.service.CategoryService;
import ru.practicum.interactionapi.dto.event.CategoryDto;
import ru.practicum.eventservice.category.dto.NewCategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto request) {
        log.info("POST /admin/categories - создание категории: {}", request);
        return categoryService.createCategory(request);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto request) {
        log.info("PATCH /admin/categories/{} - обновление категории: {}", catId, request);
        return categoryService.updateCategory(catId, request);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{} - удаление категории", catId);
        categoryService.deleteCategory(catId);
    }
}
