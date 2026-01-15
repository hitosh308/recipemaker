package com.heibonsalaryman.recipemaker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cook_log")
public class CookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cooked_at", nullable = false)
    private LocalDateTime cookedAt;

    @Column(name = "week_start")
    private LocalDate weekStart;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private Integer servings;

    @Lob
    @Column(name = "nutrition_total_json")
    private String nutritionTotalJson;

    @Lob
    @Column(name = "nutrition_per_serving_json")
    private String nutritionPerServingJson;

    @Lob
    @Column(name = "tags_json")
    private String tagsJson;

    @Lob
    @Column(name = "main_ingredients_json")
    private String mainIngredientsJson;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getCookedAt() {
        return cookedAt;
    }

    public void setCookedAt(LocalDateTime cookedAt) {
        this.cookedAt = cookedAt;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getNutritionTotalJson() {
        return nutritionTotalJson;
    }

    public void setNutritionTotalJson(String nutritionTotalJson) {
        this.nutritionTotalJson = nutritionTotalJson;
    }

    public String getNutritionPerServingJson() {
        return nutritionPerServingJson;
    }

    public void setNutritionPerServingJson(String nutritionPerServingJson) {
        this.nutritionPerServingJson = nutritionPerServingJson;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public String getMainIngredientsJson() {
        return mainIngredientsJson;
    }

    public void setMainIngredientsJson(String mainIngredientsJson) {
        this.mainIngredientsJson = mainIngredientsJson;
    }
}
