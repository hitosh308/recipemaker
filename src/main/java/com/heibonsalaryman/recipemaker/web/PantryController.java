package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.service.PantryService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        return "pantry/new";
    }

    @GetMapping("/pantry/{id}/edit")
    public String editPantryItem(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("pageTitle", "在庫編集");
        model.addAttribute("itemId", id);
        return "pantry/edit";
    }

    @PostMapping("/pantry/{id}/delete")
    public String deletePantryItem(@PathVariable("id") UUID id) {
        pantryService.deletePantryItem(id);
        return "redirect:/pantry";
    }
}
