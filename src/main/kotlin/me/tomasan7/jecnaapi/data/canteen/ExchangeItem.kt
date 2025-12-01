package me.tomasan7.jecnaapi.data.canteen

import java.time.LocalDate

data class ExchangeItem(
    val number: Int,
    val description: ItemDescription?,
    val amount: Int,
    override val orderPath: String,
    val day: LocalDate,
) : Orderable {
    /**
     * Returns a [ExchangeItem]
     *
     * Since we have to refresh the whole state after buying food from Exchange, time will be the newest at all times
     * thus we don't need to update the time
     */
    override fun updated(time: Long): ExchangeItem
    {
        return this
    }
}