package com.heibonsalaryman.recipemaker.web;

import com.heibonsalaryman.recipemaker.service.HistoryService;
import com.heibonsalaryman.recipemaker.web.dto.HistoryRowDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public String history(@RequestParam(name = "days", defaultValue = "14") int days, Model model) {
        int normalizedDays = days > 0 ? days : 14;
        List<HistoryRowDto> logs = historyService.listRecent(normalizedDays);
        model.addAttribute("logs", logs);
        model.addAttribute("days", normalizedDays);
        return "history/index";
    }

    @PostMapping("/history/{id}/delete")
    public String delete(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        historyService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "履歴を削除しました。");
        return "redirect:/history";
    }
}
