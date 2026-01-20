package io.github.tomhula.jecnaapi.data.grade

/**
 * Calculates the average based on the [grade's][Grade] weight. Takes `"small"` as weight `1` and `"big"` as weight `2`.
 */
object WeightedGradeAverageCalculator : GradeAverageCalculator
{
    override fun calculate(grades: Grades): Float?
    {
        var weightedSum = 0
        var weightSum = 0

        for (subjectPart in grades.subjectParts)
        {
            val subjectPartGrades = grades[subjectPart]!!

            for (grade in subjectPartGrades)
            {
                if (grade.value == 0)
                    continue

                val weight = if (grade.small) 1 else 2

                weightedSum += grade.value * weight
                weightSum += weight
            }
        }

        if (weightedSum == 0 || weightSum == 0)
            return null

        return weightedSum.toFloat() / weightSum
    }
}
