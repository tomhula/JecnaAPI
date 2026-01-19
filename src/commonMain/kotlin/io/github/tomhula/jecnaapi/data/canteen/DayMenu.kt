package io.github.tomhula.jecnaapi.data.canteen

import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import kotlinx.datetime.LocalDate

/**
 * A menu for a [day][LocalDate].
 */
data class DayMenu(
    val day: LocalDate,
    val items: List<MenuItem>
) : Iterable<MenuItem>
{
    override fun iterator() = items.iterator()

    companion object
    {
        @JvmStatic
        fun builder(day: LocalDate) = Builder(day)
    }

    class Builder(private val day: LocalDate)
    {
        private val items = emptyMutableLinkedList<MenuItem>()

        fun addMenuItem(menuItem: MenuItem) = items.add(menuItem)

        fun build() = DayMenu(day, items)
    }
}
