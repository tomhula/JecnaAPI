package me.tomasan7.jecnaapi.util

import java.time.DayOfWeek
import java.time.Month
import java.util.*
import kotlin.collections.HashSet

/**
 * Creates a new empty mutable [LinkedList].
 */
fun <T> emptyMutableLinkedList() = LinkedList<T>()

/**
 * @return [Month] corresponding to this number.
 * @throws java.time.DateTimeException if the month-of-year is invalid.
 * @see [Month.of]
 */
fun Int.month(): Month = Month.of(this)

/**
 * Maps any [ClosedRange] to an [IntRange] using [mappingFunction].
 */
fun <T : Comparable<T>> ClosedRange<T>.mapToIntRange(mappingFunction: (T) -> Int): IntRange
{
    val startMapped = mappingFunction(start)
    val endMapped = mappingFunction(endInclusive)

    return startMapped..endMapped
}

/**
 * Sets all [elements] to the [MutableList].
 * Shorthand for
 * ```
 * mutableList.clear()
 * mutableList.addAll(elements)
 * ```
 */
fun <T> MutableCollection<T>.setAll(elements: Iterable<T>)
{
    clear()
    addAll(elements)
}

/**
 * Returns whether there are any elements, that have the same result of [selector] function.
 */
fun <T, R> Iterable<T>.hasDuplicate(selector: (T) -> R): Boolean
{
    val set = HashSet<R>()

    for (element in this)
        if (!set.add(selector(element)))
            return true

    return false
}

/**
 * @return The next [day][DayOfWeek] after this one.
 */
fun DayOfWeek.next(): DayOfWeek = DayOfWeek.of(if (this.value == DayOfWeek.values().size) 1 else this.value + 1)

/**
 * Will strip numbers at the end of the username.
 */
fun stripNumbers(nickname: String): String {
    return nickname.dropLastWhile { it.isDigit() }
}
