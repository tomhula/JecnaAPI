package io.github.tomhula.jecnaapi.util

class SchoolYearRange(
    override val start: SchoolYear,
    override val endInclusive: SchoolYear
) : ClosedRange<SchoolYear>, Iterable<SchoolYear>
{
    override fun iterator() = SchoolYearRangeIterator(this)
}

class SchoolYearRangeIterator internal constructor(schoolYearRange: SchoolYearRange) : Iterator<SchoolYear>
{
    private val schoolYearIntIterator = (schoolYearRange.start.firstCalendarYear..schoolYearRange.endInclusive.firstCalendarYear).iterator()

    override fun hasNext() = schoolYearIntIterator.hasNext()

    override fun next() = schoolYearIntIterator.next().schoolYear()
}
