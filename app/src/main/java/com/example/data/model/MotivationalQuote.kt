package com.example.data.model

data class MotivationalMessage(
    val title: String,
    val message: String,
    val iconName: String,
    val category: String
)

object MotivationalSystem {
    val dailyQuotes = listOf(
        MotivationalMessage(
            title = "Consistency Over Perfection",
            message = "Small steps every day lead to massive results over time. Keep going!",
            iconName = "flame",
            category = "daily"
        ),
        MotivationalMessage(
            title = "Energy Follows Action",
            message = "You don't have to feel ready to begin. Start moving and the energy will come!",
            iconName = "bolt",
            category = "daily"
        ),
        MotivationalMessage(
            title = "Stronger Than Yesterday",
            message = "Every workout you complete builds resilience in your body and mind.",
            iconName = "trophy",
            category = "daily"
        ),
        MotivationalMessage(
            title = "Hydrate & Recharge",
            message = "Don't forget to hydrate well after your activity. Your muscles will thank you!",
            iconName = "water",
            category = "daily"
        )
    )

    fun getStepGoalCelebration(steps: Int, goal: Int): MotivationalMessage {
        return MotivationalMessage(
            title = "Step Target Achieved! 🏆",
            message = "Fantastic effort! You've crushed your daily goal with $steps steps. Take a moment to celebrate!",
            iconName = "celebrate",
            category = "achievement"
        )
    }

    fun getWorkoutGoalCelebration(workoutMinutes: Int): MotivationalMessage {
        return MotivationalMessage(
            title = "Workout Goal Smashed! 💪",
            message = "You've logged $workoutMinutes minutes of exercise today. You're unstoppable!",
            iconName = "workout",
            category = "achievement"
        )
    }
}
