package ru.practicum.eventservice.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.eventservice.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
