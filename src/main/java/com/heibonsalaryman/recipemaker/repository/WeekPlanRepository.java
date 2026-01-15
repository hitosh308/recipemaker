package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekPlanRepository extends JpaRepository<WeekPlan, UUID> {
    Optional<WeekPlan> findByWeekStart(LocalDate weekStart);
}
