package io.github.tomhula.jecnaapi.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

/**
 * Creates a new empty mutable [MutableList].
 */
// TODO: Remove, because a generic implementation is being provided anyways
fun <T> emptyMutableLinkedList(): MutableList<T> = mutableListOf()

/**
 * @return [Month] corresponding to this number.
 * @see [Month]
 */
fun Int.month(): Month = Month(this)

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
fun DayOfWeek.next(): DayOfWeek = DayOfWeek((this.ordinal + 1) % 7 + 1)
