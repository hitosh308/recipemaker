package com.heibonsalaryman.recipemaker.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public final class WeekUtil {

    private WeekUtil() {
    }

    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getWeekEnd(LocalDate weekStart) {
        return weekStart.plusDays(6);
    }

    public static List<WeekRange> getWeeksForMonth(YearMonth month) {
        LocalDate startOfMonth = month.atDay(1);
        LocalDate endOfMonth = month.atEndOfMonth();
        LocalDate weekStart = getWeekStart(startOfMonth);
        LocalDate lastWeekStart = getWeekStart(endOfMonth);

        List<WeekRange> ranges = new ArrayList<>();
        LocalDate cursor = weekStart;
        while (!cursor.isAfter(lastWeekStart)) {
            ranges.add(new WeekRange(cursor, getWeekEnd(cursor)));
            cursor = cursor.plusWeeks(1);
        }
        return ranges;
    }

    public record WeekRange(LocalDate weekStart, LocalDate weekEnd) {
    }
}
