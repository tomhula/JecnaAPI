package io.github.tomhula.jecnaapi.util

import io.github.tomhula.jecnaapi.data.timetable.Lesson
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UtilKtTest
{
    @Test
    fun testMapToInt()
    {
        val monthRange = Month.FEBRUARY..Month.JUNE
        val monthValueRange = Month.FEBRUARY.value..Month.JUNE.value

        assertEquals(monthValueRange, monthRange.mapToIntRange { it.value })
    }

    @Test
    fun testHasDuplicate()
    {
        val lesson1 = Lesson("Math".toName(), "C2c", "Mr. Smith".toName(), "A1", "1/3")
        val lesson2 = Lesson("English".toName(), "C2c", "Mr. Green".toName(), "B2", "1/3")
        val lesson3 = Lesson("Math".toName(), "C2c", "Mr. Smith".toName(), "C3", "3/3")

        val lessonsWithDuplicate = listOf(lesson1, lesson2, lesson3)

        assertEquals(true, lessonsWithDuplicate.hasDuplicate { it.group })

        val lessonsWithoutDuplicate = listOf(lesson1, lesson3)

        assertEquals(false, lessonsWithoutDuplicate.hasDuplicate { it.group })
    }
}
