
package me.tomasan7.jecnaapi.data.canteen

interface Orderable {
    val orderPath: String
    fun updated(time: Long): Orderable

    companion object {
        val TIME_REPLACE_REGEX = Regex("""(?<=time=)\d{13}""")
    }
}