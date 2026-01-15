package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.ai.AiService;
import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import com.heibonsalaryman.recipemaker.web.dto.PantryForm;
import com.heibonsalaryman.recipemaker.web.dto.PantryRowDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final AiService aiService;

    public PantryService(PantryItemRepository pantryItemRepository, AiService aiService) {
        this.pantryItemRepository = pantryItemRepository;
        this.aiService = aiService;
    }

    public List<PantryRowDto> listPantryItems(Optional<String> query) {
        List<PantryItem> items = query
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(pantryItemRepository::findByNameContainingIgnoreCase)
            .orElseGet(pantryItemRepository::findAll);

        LocalDate today = LocalDate.now();

        return items.stream()
            .map(item -> toRowDto(item, today))
            .sorted(Comparator
                .comparing(PantryRowDto::effectiveExpiresAt, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(PantryRowDto::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .toList();
    }

    public PantryItem getPantryItem(UUID id) {
        return pantryItemRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pantry item not found: " + id));
    }

    public PantrySaveResult createPantryItem(PantryForm form) {
        PantryItem item = new PantryItem();
        applyForm(item, form);
        boolean estimateFailed = applyShelfLifeEstimate(item);
        pantryItemRepository.save(item);
        return new PantrySaveResult(item, estimateFailed);
    }

    public PantrySaveResult updatePantryItem(UUID id, PantryForm form) {
        PantryItem item = getPantryItem(id);
        boolean shouldReestimate = shouldReestimate(item, form);
        applyForm(item, form);
        boolean estimateFailed = false;
        if (shouldReestimate) {
            estimateFailed = applyShelfLifeEstimate(item);
        }
        pantryItemRepository.save(item);
        return new PantrySaveResult(item, estimateFailed);
    }

    public void deletePantryItem(UUID id) {
        pantryItemRepository.deleteById(id);
    }

    private PantryRowDto toRowDto(PantryItem item, LocalDate today) {
        LocalDate effectiveExpiresAt = item.getExpiresAtOverride() != null
            ? item.getExpiresAtOverride()
            : item.getExpiresAtPredicted();
        Long daysToExpire = effectiveExpiresAt != null
            ? ChronoUnit.DAYS.between(today, effectiveExpiresAt)
            : null;

        return new PantryRowDto(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getUnit(),
            item.getStorageType(),
            item.getPurchasedAt(),
            effectiveExpiresAt,
            daysToExpire,
            item.getExpiresAtOverride() != null,
            item.getExpireConfidence()
        );
    }

    private void applyForm(PantryItem item, PantryForm form) {
        item.setName(form.getName());
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit());
        item.setStorageType(form.getStorageType());
        item.setPurchasedAt(form.getPurchasedAt());
        item.setExpiresAtOverride(form.getExpiresAtOverride());
    }

    private boolean shouldReestimate(PantryItem item, PantryForm form) {
        return !Objects.equals(item.getName(), form.getName())
            || item.getStorageType() != form.getStorageType()
            || !Objects.equals(item.getPurchasedAt(), form.getPurchasedAt());
    }

    private boolean applyShelfLifeEstimate(PantryItem item) {
        String assumptions = "一般的な家庭での保存。購入直後。未開封想定。日本。保存方法は入力値に従う。";
        try {
            AiService.ShelfLifeEstimate estimate = aiService.estimateShelfLifeDays(
                item.getName(),
                item.getStorageType(),
                assumptions
            );
            int days = estimate.days();
            item.setShelfLifeDaysPredicted(days);
            item.setExpiresAtPredicted(item.getPurchasedAt().plusDays(days));
            item.setExpireConfidence(estimate.confidence());
            item.setExpireAssumptions(estimate.assumptions());
            item.setExpiresEstimatedAt(LocalDateTime.now());
            return false;
        } catch (RuntimeException ex) {
            item.setShelfLifeDaysPredicted(0);
            item.setExpiresAtPredicted(item.getPurchasedAt());
            item.setExpireConfidence(null);
            item.setExpireAssumptions("期限推定に失敗しました。手動で期限を入力してください。");
            item.setExpiresEstimatedAt(LocalDateTime.now());
            return true;
        }
    }

    public record PantrySaveResult(PantryItem item, boolean estimateFailed) {
    }
}
