package com.heibonsalaryman.recipemaker.web.dto;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class PantryForm {

    @NotBlank(message = "食材名を入力してください。")
    private String name;

    @NotNull(message = "数量を入力してください。")
    @Positive(message = "数量は0より大きい値を入力してください。")
    private Double quantity;

    @NotBlank(message = "単位を入力してください。")
    private String unit;

    @NotNull(message = "保存方法を選択してください。")
    private StorageType storageType;

    @NotNull(message = "購入日を入力してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchasedAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresAtOverride;

    public static PantryForm from(PantryItem item) {
        PantryForm form = new PantryForm();
        form.setName(item.getName());
        form.setQuantity(item.getQuantity());
        form.setUnit(item.getUnit());
        form.setStorageType(item.getStorageType());
        form.setPurchasedAt(item.getPurchasedAt());
        form.setExpiresAtOverride(item.getExpiresAtOverride());
        return form;
    }

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

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public LocalDate getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDate purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public LocalDate getExpiresAtOverride() {
        return expiresAtOverride;
    }

    public void setExpiresAtOverride(LocalDate expiresAtOverride) {
        this.expiresAtOverride = expiresAtOverride;
    }
}
