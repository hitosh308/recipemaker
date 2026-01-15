package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.repository.ShoppingItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShoppingController {

    private final ShoppingItemRepository shoppingItemRepository;

    public ShoppingController(ShoppingItemRepository shoppingItemRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
    }

    @GetMapping("/shopping")
    public String shopping(Model model) {
        model.addAttribute("items", shoppingItemRepository.findAll());
        return "shopping";
    }
}
