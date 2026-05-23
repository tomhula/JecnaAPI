package io.github.tomhula.jecnaapi.data.grade

/**
 * Calculates [Grades] average.
 * Has [calculate] function.
 */
interface GradeAverageCalculator
{
    /**
     * Calculates [Grades] average.
     *
     * @param grades The [Grades] to calculate the average from.
     * @return The calculated average grade as [Float]. Or `null`, if the calculation is not possible. (eg. there are no grades)
     */
    fun calculate(grades: Grades): Float?

    operator fun invoke(grades: Grades) = calculate(grades)
}
