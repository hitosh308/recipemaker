package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import com.heibonsalaryman.recipemaker.service.PantryService;
import com.heibonsalaryman.recipemaker.web.dto.PantryForm;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PantryController {

    private final PantryService pantryService;

    public PantryController(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    @GetMapping("/pantry")
    public String pantry(@RequestParam(name = "q") Optional<String> query, Model model) {
        model.addAttribute("items", pantryService.listPantryItems(query));
        model.addAttribute("query", query.orElse(""));
        model.addAttribute("pageTitle", "在庫");
        return "pantry/index";
    }

    @GetMapping("/pantry/new")
    public String newPantryItem(Model model) {
        model.addAttribute("pageTitle", "在庫追加");
        model.addAttribute("form", new PantryForm());
        model.addAttribute("storageTypes", StorageType.values());
        return "pantry/new";
    }

    @PostMapping("/pantry/new")
    public String createPantryItem(@Valid @ModelAttribute("form") PantryForm form,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "在庫追加");
            model.addAttribute("storageTypes", StorageType.values());
            return "pantry/new";
        }
        PantryService.PantrySaveResult result = pantryService.createPantryItem(form);
        redirectAttributes.addFlashAttribute("successMessage", "在庫を追加しました。");
        if (result.estimateFailed()) {
            redirectAttributes.addFlashAttribute(
                "estimateWarning",
                "期限推定に失敗しました。手動で期限を入力してください。"
            );
        }
        return "redirect:/pantry";
    }

    @GetMapping("/pantry/{id}/edit")
    public String editPantryItem(@PathVariable("id") UUID id, Model model) {
        PantryItem item = pantryService.getPantryItem(id);
        model.addAttribute("pageTitle", "在庫編集");
        model.addAttribute("item", item);
        model.addAttribute("form", PantryForm.from(item));
        model.addAttribute("storageTypes", StorageType.values());
        return "pantry/edit";
    }

    @PostMapping("/pantry/{id}/edit")
    public String updatePantryItem(@PathVariable("id") UUID id,
                                   @Valid @ModelAttribute("form") PantryForm form,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            PantryItem item = pantryService.getPantryItem(id);
            model.addAttribute("pageTitle", "在庫編集");
            model.addAttribute("item", item);
            model.addAttribute("storageTypes", StorageType.values());
            return "pantry/edit";
        }
        PantryService.PantrySaveResult result = pantryService.updatePantryItem(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "在庫を更新しました。");
        if (result.estimateFailed()) {
            redirectAttributes.addFlashAttribute(
                "estimateWarning",
                "期限推定に失敗しました。手動で期限を入力してください。"
            );
        }
        return "redirect:/pantry";
    }

    @PostMapping("/pantry/{id}/delete")
    public String deletePantryItem(@PathVariable("id") UUID id) {
        pantryService.deletePantryItem(id);
        return "redirect:/pantry";
    }
}
