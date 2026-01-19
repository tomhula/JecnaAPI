package io.github.tomhula.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import io.github.tomhula.jecnaapi.util.Name
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.SchoolYearHalf
import io.github.tomhula.jecnaapi.util.toName
import kotlin.jvm.JvmStatic

/**
 * Representing grades table.
 * Stores `0` or more grades for each subject.
 */
@ConsistentCopyVisibility
@Serializable
data class GradesPage private constructor(
    private val subjectsMap: Map<Name, Subject>,
    val behaviour: Behaviour,
    val selectedSchoolYear: SchoolYear,
    val selectedSchoolYearHalf: SchoolYearHalf
)
{
    /** All subject names. */
    @Transient
    val subjectNames = subjectsMap.keys

    /**
     * All subjects.
     */
    @Transient
    val subjects = subjectsMap.values

    /**
     * @return [Subject] with the passed [Name]. Can be `null`, when theres no subject with that name.
     */
    fun getSubjectByName(subjectName: Name) = subjectsMap[subjectName]

    /**
     * @return [Subject] with the passed [subjectName] as it's full name. Can be `null`, when theres no subject with that name.
     */
    fun getSubjectByName(subjectName: String) = getSubjectByName(subjectName.toName())

    /**
     * @return [Subject] with the passed [Name]. Can be `null`, when theres no subject with that name.
     * @see [getSubjectByName]
     */
    operator fun get(subjectName: Name) = getSubjectByName(subjectName)

    /**
     * @return [Subject] with the passed [subjectName] as it's full name. Can be `null`, when theres no subject with that name.
     * @see [getSubjectByName]
     */
    operator fun get(subjectName: String) = getSubjectByName(subjectName)

    /**
     * This [GradesPage] as a [Map].
     * Key = subject [Name], value = [Subject].
     */
    val asMap = subjectsMap

    class Builder
    {
        private val subjects: MutableMap<Name, Subject> = HashMap()
        private lateinit var behaviour: Behaviour
        private lateinit var selectedSchoolYear: SchoolYear
        private lateinit var selectedSchoolYearHalf: SchoolYearHalf

        /**
         * Adds [Subject].
         * @param subject The subject to add.
         * @return This [Builder] instance back.
         */
        fun addSubject(subject: Subject): Builder
        {
            subjects[subject.name] = subject
            return this
        }

        fun setBehaviour(behaviour: Behaviour)
        {
            this.behaviour = behaviour
        }

        fun setSelectedSchoolYear(selectedSchoolYear: SchoolYear): Builder
        {
            this.selectedSchoolYear = selectedSchoolYear
            return this
        }

        fun setSelectedSchoolYearHalf(selectedSchoolYearHalf: SchoolYearHalf): Builder
        {
            this.selectedSchoolYearHalf = selectedSchoolYearHalf
            return this
        }

        fun build(): GradesPage
        {
            check(::behaviour.isInitialized) { "Behaviour has not been set." }
            check(::selectedSchoolYear.isInitialized) { "Selected school year has not been set." }
            check(::selectedSchoolYearHalf.isInitialized) { "Selected school year half has not been set." }

            return GradesPage(subjects, behaviour, selectedSchoolYear, selectedSchoolYearHalf)
        }
    }

    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }
}
