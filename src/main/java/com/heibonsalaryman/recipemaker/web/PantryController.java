package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PantryController {

    private final PantryItemRepository pantryItemRepository;

    public PantryController(PantryItemRepository pantryItemRepository) {
        this.pantryItemRepository = pantryItemRepository;
    }

    @GetMapping("/pantry")
    public String pantry(Model model) {
        model.addAttribute("items", pantryItemRepository.findAll());
        return "pantry";
    }
}
