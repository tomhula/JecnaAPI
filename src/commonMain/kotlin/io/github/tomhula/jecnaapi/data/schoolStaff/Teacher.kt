package io.github.tomhula.jecnaapi.data.schoolStaff

import io.github.tomhula.jecnaapi.data.SchoolAttendee
import io.github.tomhula.jecnaapi.data.timetable.Timetable
import java.net.URI

class Teacher(
    fullName: String,
    username: String,
    schoolMail: String,
    privateMail: String? = null,
    phoneNumbers: List<String> = emptyList(),
    profilePicturePath: String? = null,
    tag: String,
    val privatePhoneNumber: String? = null,
    val landline: String? = null,
    val cabinet: String? = null,
    val tutorOfClass: String? = null,
    val consultationHours: String? = null,
    val timetable: Timetable? = null
) : SchoolAttendee(fullName, username, schoolMail, privateMail, phoneNumbers, profilePicturePath)
{
    val tag = tag.trim().lowercase().replaceFirstChar { it.uppercaseChar() }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Teacher

        if (privatePhoneNumber != other.privatePhoneNumber) return false
        if (landline != other.landline) return false
        if (cabinet != other.cabinet) return false
        if (tutorOfClass != other.tutorOfClass) return false
        if (consultationHours != other.consultationHours) return false
        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = privatePhoneNumber?.hashCode() ?: 0
        result = 31 * result + (landline?.hashCode() ?: 0)
        result = 31 * result + (cabinet?.hashCode() ?: 0)
        result = 31 * result + (tutorOfClass?.hashCode() ?: 0)
        result = 31 * result + (consultationHours?.hashCode() ?: 0)
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Teacher(" +
                "fullName='$fullName', " +
                "username='$username', " +
                "schoolMail='$schoolMail', " +
                "privateMail=$privateMail, " +
                "phoneNumbers=$phoneNumbers, " +
                "profilePicturePath=$profilePicturePath, " +
                "privatePhoneNumber=$privatePhoneNumber, " +
                "landline=$landline, " +
                "cabinet=$cabinet, " +
                "tutorOfClass=$tutorOfClass, " +
                "consultationHours=$consultationHours, " +
                "tag='$tag'" +
                ")"
    }
}
