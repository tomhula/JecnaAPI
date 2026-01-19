package io.github.tomhula.jecnaapi.data.canteen

import kotlinx.datetime.LocalDate

/**
 * A food [Menu] for X number of days.
 * Is mutable and is supposed to be changed with [replace] function as user orders [MenuItems][MenuItem].
 */
class Menu(menu: Map<LocalDate, DayMenu>) : Iterable<DayMenu>
{
    private val menu = menu.toMutableMap()

    val days: Set<LocalDate>
        get() = menu.keys.toSet()

    val dayMenus: Set<DayMenu>
        get() = menu.values.toSet()

    /**
     * @return a [DayMenu] for [day].
     */
    fun getDayMenu(day: LocalDate) = menu[day]

    /**
     * @return a [DayMenu] for [day].
     */
    operator fun get(day: LocalDate) = getDayMenu(day)

    /**
     * Replaces the [day's][day] [DayMenu] with th provided [dayMenu].
     * Should be called with a new [DayMenu] received from the server when user orders a [MenuItem] in the [dayMenu].
     */
    fun replace(day: LocalDate, dayMenu: DayMenu) = menu.replace(day, dayMenu)

    override fun iterator() = dayMenus.iterator()

    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val menu = mutableMapOf<LocalDate, DayMenu>()

        fun addDayMenu(day: LocalDate, dayMenu: DayMenu)
        {
            menu[day] = dayMenu
        }

        fun build() = Menu(menu)
    }
}
