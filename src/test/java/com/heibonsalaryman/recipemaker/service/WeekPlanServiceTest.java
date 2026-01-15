package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.ai.AiService;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import com.heibonsalaryman.recipemaker.repository.RecipeRepository;
import com.heibonsalaryman.recipemaker.repository.WeekCandidateRepository;
import com.heibonsalaryman.recipemaker.repository.WeekPlanRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeekPlanServiceTest {

    @Mock
    private WeekPlanRepository weekPlanRepository;
    @Mock
    private WeekCandidateRepository weekCandidateRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private PantryItemRepository pantryItemRepository;
    @Mock
    private CookLogRepository cookLogRepository;
    @Mock
    private AiContextBuilder aiContextBuilder;
    @Mock
    private AiService aiService;

    @InjectMocks
    private WeekPlanService weekPlanService;

    @Captor
    private ArgumentCaptor<WeekCandidate> weekCandidateCaptor;

    @Test
    void regenerateIncrementsCandidateGroupVersion() {
        LocalDate weekStart = LocalDate.of(2024, 1, 1);
        WeekPlan plan = new WeekPlan();
        plan.setWeekStart(weekStart);
        plan.setStatus(WeekStatus.GENERATING);

        WeekCandidate latest = new WeekCandidate();
        latest.setCandidateGroupVersion(2);
        when(weekPlanRepository.findByWeekStart(weekStart)).thenReturn(Optional.of(plan));
        when(weekCandidateRepository.findTopByWeekPlanOrderByCandidateGroupVersionDesc(plan))
            .thenReturn(Optional.of(latest));
        when(aiContextBuilder.buildPantryContext(any(), any(), anyInt())).thenReturn("[]");
        when(aiContextBuilder.buildRecentHistory(any())).thenReturn("[]");
        when(aiContextBuilder.defaultConstraints()).thenReturn("constraints");
        Recipe recipe = new Recipe();
        recipe.setTitle("Test");
        when(aiService.generateWeeklyRecipeCandidates(any(), any(), any(), any())).thenReturn(List.of(recipe));

        weekPlanService.regenerate(weekStart);

        verify(weekCandidateRepository).save(weekCandidateCaptor.capture());
        assertThat(weekCandidateCaptor.getValue().getCandidateGroupVersion()).isEqualTo(3);
    }
}
