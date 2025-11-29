package me.tomasan7.jecnaapi.data.canteen

data class MenuItem(
    val number: Int,
    val description: ItemDescription?,
    val allergens: List<String>? = null,
    val price: Float,
    val isEnabled: Boolean,
    val isOrdered: Boolean,
    val isInExchange: Boolean,
    val orderPath: String,
    val putOnExchangePath: String? = null,
    val putAwayFromExchangePath: String? = null,
)
