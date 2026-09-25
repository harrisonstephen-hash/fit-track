package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

data class BreakfastMeal(
    val id: String,
    val title: String,
    val tagline: String,
    @DrawableRes val imageRes: Int,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val prepTimeMinutes: Int,
    val tags: List<String>,
    val description: String,
    val ingredients: List<String>,
    val benefits: String
)

object BreakfastCatalog {
    val healthyBreakfasts: List<BreakfastMeal> = listOf(
        BreakfastMeal(
            id = "oatmeal_berries",
            title = "Antioxidant Berry Oatmeal",
            tagline = "High Fiber & Sustained Energy",
            imageRes = R.drawable.breakfast_oatmeal,
            calories = 360,
            proteinGrams = 14,
            carbsGrams = 58,
            fatGrams = 7,
            prepTimeMinutes = 8,
            tags = listOf("High Fiber", "Antioxidant", "Heart Healthy"),
            description = "Warm rolled oats simmered in unsweetened almond milk, loaded with fresh blueberries, sliced bananas, organic chia seeds, and raw blossom honey.",
            ingredients = listOf(
                "1 cup rolled whole oats",
                "1 cup unsweetened almond milk",
                "1/2 cup fresh wild blueberries",
                "1 sliced ripe banana",
                "1 tbsp chia seeds",
                "1 tsp raw organic honey"
            ),
            benefits = "Provides slow-burning complex carbs for steady morning endurance, beta-glucan to lower cholesterol, and potent anthocyanins for cellular recovery."
        ),
        BreakfastMeal(
            id = "avocado_poached_egg",
            title = "Poached Egg & Sourdough Toast",
            tagline = "Essential Fats & Muscle Recovery",
            imageRes = R.drawable.breakfast_toast,
            calories = 410,
            proteinGrams = 19,
            carbsGrams = 34,
            fatGrams = 22,
            prepTimeMinutes = 12,
            tags = listOf("High Protein", "Healthy Fats", "Keto Friendly"),
            description = "Crispy toasted artisan sourdough layered with seasoned mashed Hass avocado, two pasture-raised soft-poached eggs, microgreens, and red pepper flakes.",
            ingredients = listOf(
                "2 slices artisan sourdough bread",
                "1/2 ripe Hass avocado",
                "2 organic pasture-raised eggs",
                "1 pinch flaky sea salt & black pepper",
                "Fresh microgreens / arugula",
                "Chili flakes & cold-pressed olive oil drizzle"
            ),
            benefits = "Packed with lutein, omega-9 monounsaturated fatty acids, and complete amino acids that trigger muscle protein synthesis after morning workouts."
        ),
        BreakfastMeal(
            id = "greek_yogurt_crunch",
            title = "Layered Greek Yogurt Parfait",
            tagline = "Gut Health & High Protein Boost",
            imageRes = R.drawable.breakfast_parfait,
            calories = 320,
            proteinGrams = 24,
            carbsGrams = 38,
            fatGrams = 6,
            prepTimeMinutes = 5,
            tags = listOf("Gut Health", "24g Protein", "No Cook"),
            description = "Thick, probiotic-packed authentic 0% Greek yogurt layered with toasted almond granola, golden flaxseeds, fresh raspberries, and pure maple syrup.",
            ingredients = listOf(
                "1 cup plain low-fat Greek yogurt (0-2%)",
                "1/3 cup whole rolled grain granola",
                "1/2 cup fresh organic raspberries",
                "1 tbsp toasted pumpkin & flax seeds",
                "1 tsp pure amber maple syrup"
            ),
            benefits = "Supplies active probiotic cultures to strengthen gut flora and microbiome, alongside 24g of slow-digesting casein protein."
        ),
        BreakfastMeal(
            id = "protein_banana_pancakes",
            title = "Golden Banana Protein Pancakes",
            tagline = "Pre/Post Workout Power Fuel",
            imageRes = R.drawable.breakfast_pancakes,
            calories = 440,
            proteinGrams = 28,
            carbsGrams = 52,
            fatGrams = 9,
            prepTimeMinutes = 15,
            tags = listOf("Pre-Workout", "28g Protein", "Gluten-Free"),
            description = "Fluffy gluten-free banana oat protein pancakes cooked in coconut oil, crowned with fresh ripe strawberries, sliced bananas, and cinnamon.",
            ingredients = listOf(
                "1 scoop vanilla whey or plant protein powder",
                "1 medium ripe banana mashed",
                "1/2 cup blended oat flour",
                "1 large egg or egg whites",
                "1/4 cup almond milk",
                "1/2 cup fresh sliced strawberries"
            ),
            benefits = "Fast-absorbing glycogen replenishment and branched-chain amino acids (BCAAs) that accelerate muscular recovery and reduce soreness."
        )
    )
}
