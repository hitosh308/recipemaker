package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final CookLogRepository cookLogRepository;

    public HealthService(CookLogRepository cookLogRepository) {
        this.cookLogRepository = cookLogRepository;
    }

    public List<DailyHealthSummary> summarizeLastDays(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        List<CookLog> logs = cookLogRepository.findByCookedAtBetween(from, to);

        Map<LocalDate, DailyHealthSummary> summaries = new TreeMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            summaries.put(cursor, new DailyHealthSummary(cursor, 0, 0, 0));
            cursor = cursor.plusDays(1);
        }

        for (CookLog log : logs) {
            LocalDate date = log.getCookedAt().toLocalDate();
            DailyHealthSummary summary = summaries.get(date);
            if (summary == null) {
                continue;
            }
            int servings = log.getServings() == null ? 1 : log.getServings();
            summary.add(servings * 500, servings * 25, servings * 2);
        }

        return new ArrayList<>(summaries.values());
    }

    public static class DailyHealthSummary {
        private final LocalDate date;
        private int calories;
        private int protein;
        private int salt;

        public DailyHealthSummary(LocalDate date, int calories, int protein, int salt) {
            this.date = date;
            this.calories = calories;
            this.protein = protein;
            this.salt = salt;
        }

        public void add(int calories, int protein, int salt) {
            this.calories += calories;
            this.protein += protein;
            this.salt += salt;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getCalories() {
            return calories;
        }

        public int getProtein() {
            return protein;
        }

        public int getSalt() {
            return salt;
        }
    }
}
