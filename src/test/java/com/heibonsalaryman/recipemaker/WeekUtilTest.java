package com.heibonsalaryman.recipemaker;

import com.heibonsalaryman.recipemaker.util.WeekUtil;
import com.heibonsalaryman.recipemaker.util.WeekUtil.WeekRange;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeekUtilTest {

    @Test
    void generatesWeekRangesIncludingCrossMonthWeeks() {
        YearMonth month = YearMonth.of(2024, 5);
        List<WeekRange> ranges = WeekUtil.getWeeksForMonth(month);

        assertThat(ranges).isNotEmpty();
        WeekRange first = ranges.get(0);
        WeekRange last = ranges.get(ranges.size() - 1);

        assertThat(first.weekStart().getDayOfWeek().name()).isEqualTo("MONDAY");
        assertThat(first.weekEnd()).isEqualTo(first.weekStart().plusDays(6));
        assertThat(last.weekStart()).isBeforeOrEqualTo(month.atEndOfMonth());
        assertThat(last.weekEnd()).isAfterOrEqualTo(month.atEndOfMonth());
    }

    @Test
    void weekEndIsSixDaysAfterWeekStart() {
        LocalDate date = LocalDate.of(2024, 4, 10);
        LocalDate weekStart = WeekUtil.getWeekStart(date);
        LocalDate weekEnd = WeekUtil.getWeekEnd(weekStart);

        assertThat(weekEnd).isEqualTo(weekStart.plusDays(6));
    }
}
