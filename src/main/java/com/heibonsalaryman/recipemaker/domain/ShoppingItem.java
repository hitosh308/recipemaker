package com.heibonsalaryman.recipemaker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "shopping_item")
public class ShoppingItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private Double quantity;

    private String unit;

    @Column(nullable = false)
    private Boolean checked = Boolean.FALSE;

    @Column(name = "shelf_life_days_hint")
    private Integer shelfLifeDaysHint;

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

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Integer getShelfLifeDaysHint() {
        return shelfLifeDaysHint;
    }

    public void setShelfLifeDaysHint(Integer shelfLifeDaysHint) {
        this.shelfLifeDaysHint = shelfLifeDaysHint;
    }
}
