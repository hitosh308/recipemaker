package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.domain.StorageType;
import com.heibonsalaryman.recipemaker.service.PantryService;
import com.heibonsalaryman.recipemaker.service.ShoppingService;
import com.heibonsalaryman.recipemaker.web.dto.ShoppingForm;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @GetMapping("/shopping")
    public String shopping(Model model) {
        model.addAttribute("items", shoppingService.list());
        model.addAttribute("form", new ShoppingForm());
        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("pageTitle", "買い物リスト");
        return "shopping/index";
    }

    @PostMapping("/shopping/add")
    public String add(@Valid @ModelAttribute("form") ShoppingForm form,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("items", shoppingService.list());
            model.addAttribute("storageTypes", StorageType.values());
            model.addAttribute("pageTitle", "買い物リスト");
            return "shopping/index";
        }
        shoppingService.addOrMerge(form.getName(), form.getQuantity(), form.getUnit(), null);
        redirectAttributes.addFlashAttribute("successMessage", "買い物リストに追加しました。");
        return "redirect:/shopping";
    }

    @PostMapping("/shopping/{id}/toggle")
    public String toggle(@PathVariable("id") UUID id) {
        shoppingService.toggleChecked(id);
        return "redirect:/shopping";
    }

    @PostMapping("/shopping/{id}/delete")
    public String delete(@PathVariable("id") UUID id,
                         RedirectAttributes redirectAttributes) {
        shoppingService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "買い物リストから削除しました。");
        return "redirect:/shopping";
    }

    @PostMapping("/shopping/{id}/move-to-pantry")
    public String moveToPantry(@PathVariable("id") UUID id,
                               @RequestParam("storageType") StorageType storageType,
                               RedirectAttributes redirectAttributes) {
        PantryService.PantrySaveResult result = shoppingService.moveToPantry(id, storageType);
        redirectAttributes.addFlashAttribute("successMessage", "在庫へ追加しました。");
        if (result.estimateFailed()) {
            redirectAttributes.addFlashAttribute(
                "estimateWarning",
                "期限推定に失敗しました。手動で期限を入力してください。"
            );
        }
        return "redirect:/shopping";
    }
}
