package com.heibonsalaryman.recipemaker.service;

import com.heibonsalaryman.recipemaker.domain.CookLog;
import com.heibonsalaryman.recipemaker.domain.PantryItem;
import com.heibonsalaryman.recipemaker.domain.Recipe;
import com.heibonsalaryman.recipemaker.domain.ShoppingItem;
import com.heibonsalaryman.recipemaker.domain.StorageType;
import com.heibonsalaryman.recipemaker.domain.WeekCandidate;
import com.heibonsalaryman.recipemaker.domain.WeekPlan;
import com.heibonsalaryman.recipemaker.domain.WeekStatus;
import com.heibonsalaryman.recipemaker.repository.CookLogRepository;
import com.heibonsalaryman.recipemaker.repository.PantryItemRepository;
import com.heibonsalaryman.recipemaker.repository.RecipeRepository;
import com.heibonsalaryman.recipemaker.repository.ShoppingItemRepository;
import com.heibonsalaryman.recipemaker.repository.WeekCandidateRepository;
import com.heibonsalaryman.recipemaker.repository.WeekPlanRepository;
import com.heibonsalaryman.recipemaker.util.WeekUtil;
import com.heibonsalaryman.recipemaker.util.WeekUtil.WeekRange;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final WeekPlanRepository weekPlanRepository;
    private final WeekCandidateRepository weekCandidateRepository;
    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final CookLogRepository cookLogRepository;

    public DataSeeder(WeekPlanRepository weekPlanRepository,
                      WeekCandidateRepository weekCandidateRepository,
                      RecipeRepository recipeRepository,
                      PantryItemRepository pantryItemRepository,
                      ShoppingItemRepository shoppingItemRepository,
                      CookLogRepository cookLogRepository) {
        this.weekPlanRepository = weekPlanRepository;
        this.weekCandidateRepository = weekCandidateRepository;
        this.recipeRepository = recipeRepository;
        this.pantryItemRepository = pantryItemRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.cookLogRepository = cookLogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (weekPlanRepository.count() > 0) {
            return;
        }

        List<WeekRange> ranges = WeekUtil.getWeeksForMonth(YearMonth.now());
        if (ranges.size() < 3) {
            return;
        }

        WeekPlan notCreated = buildWeekPlan(ranges.get(0), WeekStatus.NOT_CREATED);
        WeekPlan generating = buildWeekPlan(ranges.get(1), WeekStatus.GENERATING);
        WeekPlan confirmed = buildWeekPlan(ranges.get(2), WeekStatus.CONFIRMED);

        weekPlanRepository.save(notCreated);
        weekPlanRepository.save(generating);
        weekPlanRepository.save(confirmed);

        Recipe confirmedRecipe = createRecipe("Confirmed Curry", "Manual");
        recipeRepository.save(confirmedRecipe);
        confirmed.setConfirmedRecipe(confirmedRecipe);
        weekPlanRepository.save(confirmed);

        createCandidateSet(generating, 1, List.of(
            createRecipe("Candidate A", "AI"),
            createRecipe("Candidate B", "AI"),
            createRecipe("Candidate C", "AI")
        ));

        PantryItem milk = new PantryItem();
        milk.setName("Milk");
        milk.setQuantity(1.0);
        milk.setUnit("L");
        milk.setStorageType(StorageType.FRIDGE);
        milk.setPurchasedAt(LocalDate.now().minusDays(2));
        milk.setShelfLifeDaysPredicted(7);
        milk.setExpiresAtPredicted(LocalDate.now().plusDays(5));
        milk.setExpireConfidence(0.8);
        milk.setExpireAssumptions("Opened package");
        milk.setExpiresEstimatedAt(LocalDateTime.now());

        PantryItem rice = new PantryItem();
        rice.setName("Rice");
        rice.setQuantity(2.0);
        rice.setUnit("kg");
        rice.setStorageType(StorageType.ROOM);
        rice.setPurchasedAt(LocalDate.now().minusDays(10));
        rice.setShelfLifeDaysPredicted(90);
        rice.setExpiresAtPredicted(LocalDate.now().plusDays(80));

        pantryItemRepository.saveAll(List.of(milk, rice));

        ShoppingItem carrot = new ShoppingItem();
        carrot.setName("Carrot");
        carrot.setQuantity(3.0);
        carrot.setUnit("pcs");
        carrot.setChecked(false);
        carrot.setShelfLifeDaysHint(7);

        ShoppingItem chicken = new ShoppingItem();
        chicken.setName("Chicken breast");
        chicken.setQuantity(2.0);
        chicken.setUnit("pcs");
        chicken.setChecked(true);
        chicken.setShelfLifeDaysHint(3);

        shoppingItemRepository.saveAll(List.of(carrot, chicken));

        createCookLog(confirmedRecipe, LocalDateTime.now().minusDays(2), 2);
        createCookLog(confirmedRecipe, LocalDateTime.now().minusDays(5), 3);
        createCookLog(confirmedRecipe, LocalDateTime.now().minusDays(10), 1);
    }

    private WeekPlan buildWeekPlan(WeekRange range, WeekStatus status) {
        WeekPlan plan = new WeekPlan();
        plan.setWeekStart(range.weekStart());
        plan.setWeekEnd(range.weekEnd());
        plan.setStatus(status);
        return plan;
    }

    private Recipe createRecipe(String title, String source) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setServings(2);
        recipe.setCookTimeMinutes(25);
        recipe.setStepsJson("[\"Prep\", \"Cook\"]");
        recipe.setIngredientsJson("[\"Ingredient 1\", \"Ingredient 2\"]");
        recipe.setMissingIngredientsJson("[]");
        recipe.setNutritionPerServingJson("{\"calories\":450}");
        recipe.setTagsJson("[\"seed\"]");
        recipe.setMainIngredientsJson("[\"Chicken\"]");
        recipe.setSource(source);
        return recipe;
    }

    private void createCandidateSet(WeekPlan plan, int version, List<Recipe> recipes) {
        recipeRepository.saveAll(recipes);
        for (Recipe recipe : recipes) {
            WeekCandidate candidate = new WeekCandidate();
            candidate.setWeekPlan(plan);
            candidate.setRecipe(recipe);
            candidate.setCandidateGroupVersion(version);
            weekCandidateRepository.save(candidate);
        }
    }

    private void createCookLog(Recipe recipe, LocalDateTime cookedAt, int servings) {
        CookLog log = new CookLog();
        log.setRecipe(recipe);
        log.setCookedAt(cookedAt);
        log.setWeekStart(WeekUtil.getWeekStart(cookedAt.toLocalDate()));
        log.setServings(servings);
        log.setNutritionTotalJson("{\"calories\":" + (servings * 500) + "}");
        log.setNutritionPerServingJson("{\"calories\":500}");
        log.setTagsJson("[\"seed\"]");
        log.setMainIngredientsJson("[\"Chicken\"]");
        cookLogRepository.save(log);
    }
}
