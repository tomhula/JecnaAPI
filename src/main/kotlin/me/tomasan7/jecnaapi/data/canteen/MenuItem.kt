package me.tomasan7.jecnaapi.data.canteen

data class MenuItem(
    val number: Int,
    val description: ItemDescription?,
    val allergens: List<String>? = null,
    val price: Float,
    val isEnabled: Boolean,
    val isOrdered: Boolean,
    val isInExchange: Boolean,
    override val orderPath: String,
    val putOnExchangePath: String? = null,
) : Orderable 
{

    /**
     * Returns a [MenuItem] with updated time in the [MenuItem.orderPath] and possibly [MenuItem.putOnExchangePath].
     */
    override fun updated(time: Long): MenuItem
    {
        val newOrderPath = orderPath.replace(Orderable.TIME_REPLACE_REGEX, time.toString())
        val newPutOnExchangePath = putOnExchangePath?.replace(Orderable.TIME_REPLACE_REGEX, time.toString())
        return copy(orderPath = newOrderPath, putOnExchangePath = newPutOnExchangePath)
    }
}
