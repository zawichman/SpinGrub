package com.spingrub.app.data

/** The three spinner categories. Order matters for UI layout. */
enum class Category(val title: String, val emoji: String) {
    MEAT("Meat", "\uD83E\uDD69"),   // 🥩
    METHOD("Base", "\uD83E\uDD57"), // 🥗  (how you eat it: bowl / bread / salad …)
    SAUCE("Sauce", "\uD83E\uDED9"); // 🫙

    companion object {
        val ordered = listOf(MEAT, METHOD, SAUCE)
    }
}

/** A saved favorite combo of the three landed items. */
data class Favorite(
    val id: String,
    val name: String,
    val meat: String,
    val method: String,
    val sauce: String
) {
    val recipe: List<String> get() = listOf(meat, method, sauce)
}

/** Full app state persisted to disk. */
data class SpinGrubData(
    val meat: List<String> = DefaultData.MEAT,
    val method: List<String> = DefaultData.METHOD,
    val sauce: List<String> = DefaultData.SAUCE,
    val favorites: List<Favorite> = emptyList(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val confettiEnabled: Boolean = true,
) {
    fun itemsFor(category: Category): List<String> = when (category) {
        Category.MEAT -> meat
        Category.METHOD -> method
        Category.SAUCE -> sauce
    }
}

object DefaultData {
    val MEAT = listOf("Chicken", "Beef", "Pork", "Tofu", "Shrimp", "Salmon", "Lamb", "Turkey")

    // "Base" = the way you serve/eat the meat.
    val METHOD = listOf(
        "Rice Bowl", "Salad", "Sandwich", "Wrap", "Tacos",
        "Pasta", "Burger", "Flatbread", "Noodles", "Loaded Fries"
    )

    val SAUCE = listOf("BBQ", "Teriyaki", "Garlic Butter", "Peri-Peri", "Honey Mustard", "Sriracha", "Chimichurri", "Alfredo")
}
