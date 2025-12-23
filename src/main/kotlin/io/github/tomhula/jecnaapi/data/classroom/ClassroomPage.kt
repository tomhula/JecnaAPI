package io.github.tomhula.jecnaapi.data.classroom

data class ClassroomPage(val classroomRefs: Set<ClassroomReference>)
{
    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val classroomRefs = mutableSetOf<ClassroomReference>()

        fun addClassroomReference(classroomRef: ClassroomReference): Builder
        {
            classroomRefs.add(classroomRef)
            return this
        }

        fun build() = ClassroomPage(classroomRefs)
    }
}
