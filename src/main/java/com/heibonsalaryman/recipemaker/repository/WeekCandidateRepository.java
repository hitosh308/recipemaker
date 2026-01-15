package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekCandidateRepository extends JpaRepository<WeekCandidate, UUID> {
    Optional<WeekCandidate> findTopByWeekPlanOrderByCandidateGroupVersionDesc(WeekPlan weekPlan);

    List<WeekCandidate> findByWeekPlanAndCandidateGroupVersion(WeekPlan weekPlan, Integer candidateGroupVersion);
}
