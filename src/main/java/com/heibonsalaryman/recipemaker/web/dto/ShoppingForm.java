package com.heibonsalaryman.recipemaker.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ShoppingForm {

    @NotBlank(message = "食材名を入力してください。")
    private String name;

    @NotNull(message = "数量を入力してください。")
    @Positive(message = "数量は0より大きい値を入力してください。")
    private Double quantity;

    @NotBlank(message = "単位を入力してください。")
    private String unit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
