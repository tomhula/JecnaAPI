package io.github.tomhula.jecnaapi.data.room

data class RoomsPage(val roomReferences: Set<RoomReference>)
{
    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val roomReferences = mutableSetOf<RoomReference>()

        fun addRoomReference(roomReference: RoomReference): Builder
        {
            roomReferences.add(roomReference)
            return this
        }

        fun build() = RoomsPage(roomReferences)
    }
}
