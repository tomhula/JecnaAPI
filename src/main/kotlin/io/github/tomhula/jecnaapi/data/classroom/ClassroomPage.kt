package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference

class ClassroomPage(val classroomRefs: Set<TeacherReference>)
{
    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val classroomRefs = mutableSetOf<TeacherReference>()

        fun addClassroomReference(classroomRef: TeacherReference): Builder
        {
            classroomRefs.add(classroomRef)
            return this
        }

        fun build() = ClassroomPage(classroomRefs)
    }
}
