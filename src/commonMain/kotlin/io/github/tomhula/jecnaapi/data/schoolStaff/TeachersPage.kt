package io.github.tomhula.jecnaapi.data.schoolStaff

import io.github.tomhula.jecnaapi.util.setAll

data class TeachersPage(val teachersReferences: Set<TeacherReference>)
{
    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val teacherReferences = mutableSetOf<TeacherReference>()

        fun addTeacherReference(teacherReference: TeacherReference): Builder
        {
            teacherReferences.add(teacherReference)
            return this
        }

        fun setArticles(teachersReferences: Set<TeacherReference>): Builder
        {
            this.teacherReferences.setAll(teachersReferences)
            return this
        }

        fun build() = TeachersPage(teacherReferences)
    }
}
