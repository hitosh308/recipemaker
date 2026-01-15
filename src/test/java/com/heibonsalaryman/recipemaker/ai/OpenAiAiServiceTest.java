package com.heibonsalaryman.recipemaker.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiAiServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void estimateShelfLifeFallsBackOnInvalidJson() {
        when(openAiClient.createJsonResponse(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenReturn("not-json");
        OpenAiAiService service = new OpenAiAiService(openAiClient, objectMapper);

        AiService.ShelfLifeEstimate estimate = service.estimateShelfLifeDays(
            "Milk",
            StorageType.FRIDGE,
            "assumptions"
        );

        assertThat(estimate.days()).isEqualTo(0);
        assertThat(estimate.confidence()).isEqualTo(0.0);
    }

    @Test
    void generateWeeklyRecipeCandidatesFallsBackOnInvalidJson() {
        when(openAiClient.createJsonResponse(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenReturn("not-json");
        OpenAiAiService service = new OpenAiAiService(openAiClient, objectMapper);

        List<Recipe> recipes = service.generateWeeklyRecipeCandidates(LocalDate.of(2024, 1, 1), null, null, null);

        assertThat(recipes).hasSizeGreaterThanOrEqualTo(3);
    }
}
