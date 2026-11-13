package com.tinybakery.rush.domain.model

enum class RecipeId { BUN, CROISSANT, DANISH, SWIRL }
data class Recipe(val id: RecipeId, val fromDay: Int, val bakeMs: Long, val coins: Int, val taps: Int = 3)
enum class CustomerId { MILA, OMAR, NORA }
