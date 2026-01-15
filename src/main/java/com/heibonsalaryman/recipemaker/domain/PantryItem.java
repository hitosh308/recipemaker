package com.heibonsalaryman.recipemaker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pantry_item")
public class PantryItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private Double quantity;

    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type")
    private StorageType storageType;

    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    @Column(name = "shelf_life_days_predicted")
    private Integer shelfLifeDaysPredicted;

    @Column(name = "expires_at_predicted")
    private LocalDate expiresAtPredicted;

    @Column(name = "expires_at_override")
    private LocalDate expiresAtOverride;

    @Column(name = "expire_confidence")
    private Double expireConfidence;

    @Lob
    @Column(name = "expire_assumptions")
    private String expireAssumptions;

    @Column(name = "expire_estimated_at")
    private LocalDateTime expiresEstimatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Integer getShelfLifeDaysPredicted() {
        return shelfLifeDaysPredicted;
    }

    public void setShelfLifeDaysPredicted(Integer shelfLifeDaysPredicted) {
        this.shelfLifeDaysPredicted = shelfLifeDaysPredicted;
    }

    public LocalDate getExpiresAtPredicted() {
        return expiresAtPredicted;
    }

    public void setExpiresAtPredicted(LocalDate expiresAtPredicted) {
        this.expiresAtPredicted = expiresAtPredicted;
    }

    public LocalDate getExpiresAtOverride() {
        return expiresAtOverride;
    }

    public void setExpiresAtOverride(LocalDate expiresAtOverride) {
        this.expiresAtOverride = expiresAtOverride;
    }

    public Double getExpireConfidence() {
        return expireConfidence;
    }

    public void setExpireConfidence(Double expireConfidence) {
        this.expireConfidence = expireConfidence;
    }

    public String getExpireAssumptions() {
        return expireAssumptions;
    }

    public void setExpireAssumptions(String expireAssumptions) {
        this.expireAssumptions = expireAssumptions;
    }

    public LocalDateTime getExpiresEstimatedAt() {
        return expiresEstimatedAt;
    }

    public void setExpiresEstimatedAt(LocalDateTime expiresEstimatedAt) {
        this.expiresEstimatedAt = expiresEstimatedAt;
    }
}
