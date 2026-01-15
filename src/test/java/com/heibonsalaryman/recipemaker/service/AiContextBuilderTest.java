package com.heibonsalaryman.recipemaker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiContextBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiContextBuilder builder = new AiContextBuilder(objectMapper);

    @Test
    void buildPantryContextSortsByEffectiveExpiry() throws Exception {
        PantryItem soon = new PantryItem();
        soon.setName("Milk");
        soon.setQuantity(1.0);
        soon.setUnit("L");
        soon.setStorageType(StorageType.FRIDGE);
        soon.setExpiresAtPredicted(LocalDate.of(2024, 1, 3));

        PantryItem later = new PantryItem();
        later.setName("Rice");
        later.setQuantity(2.0);
        later.setUnit("kg");
        later.setStorageType(StorageType.ROOM);
        later.setExpiresAtPredicted(LocalDate.of(2024, 2, 1));

        PantryItem override = new PantryItem();
        override.setName("Spinach");
        override.setQuantity(1.0);
        override.setUnit("bag");
        override.setStorageType(StorageType.FRIDGE);
        override.setExpiresAtPredicted(LocalDate.of(2024, 1, 10));
        override.setExpiresAtOverride(LocalDate.of(2024, 1, 2));

        String json = builder.buildPantryContext(List.of(later, override, soon), LocalDate.of(2024, 1, 1), 30);
        List<AiContextBuilder.PantryContextItem> items = objectMapper.readValue(
            json, new TypeReference<List<AiContextBuilder.PantryContextItem>>() {
            });

        assertThat(items).hasSize(3);
        assertThat(items.get(0).name()).isEqualTo("Spinach");
        assertThat(items.get(1).name()).isEqualTo("Milk");
        assertThat(items.get(2).name()).isEqualTo("Rice");
    }
}
