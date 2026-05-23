package io.github.tomhula.jecnaapi.util

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SchoolYearRangeTest
{
    @Test
    fun testSchoolYearRange()
    {
        val actual = (2020.schoolYear()..2024.schoolYear()).toList()
        val expected = listOf(
            2020.schoolYear(),
            2021.schoolYear(),
            2022.schoolYear(),
            2023.schoolYear(),
            2024.schoolYear(),
        )

        assertEquals(expected, actual)

        assertEquals(emptyList(), (2022.schoolYear()..2020.schoolYear()).toList())
    }
}
