package com.heibonsalaryman.recipemaker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private Integer servings;

    @Column(name = "cook_time_minutes")
    private Integer cookTimeMinutes;

    @Lob
    @Column(name = "steps_json")
    private String stepsJson;

    @Lob
    @Column(name = "ingredients_json")
    private String ingredientsJson;

    @Lob
    @Column(name = "missing_ingredients_json")
    private String missingIngredientsJson;

    @Lob
    @Column(name = "nutrition_per_serving_json")
    private String nutritionPerServingJson;

    @Lob
    @Column(name = "tags_json")
    private String tagsJson;

    @Lob
    @Column(name = "main_ingredients_json")
    private String mainIngredientsJson;

    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public String getStepsJson() {
        return stepsJson;
    }

    public void setStepsJson(String stepsJson) {
        this.stepsJson = stepsJson;
    }

    public String getIngredientsJson() {
        return ingredientsJson;
    }

    public void setIngredientsJson(String ingredientsJson) {
        this.ingredientsJson = ingredientsJson;
    }

    public String getMissingIngredientsJson() {
        return missingIngredientsJson;
    }

    public void setMissingIngredientsJson(String missingIngredientsJson) {
        this.missingIngredientsJson = missingIngredientsJson;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
