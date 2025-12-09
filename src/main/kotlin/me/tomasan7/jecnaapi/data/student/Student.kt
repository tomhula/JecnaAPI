package me.tomasan7.jecnaapi.data.student

import me.tomasan7.jecnaapi.data.SchoolAttendee
import java.time.LocalDate

class Student(
    fullName: String,
    username: String,
    schoolMail: String,
    privateMail: String? = null,
    phoneNumbers: List<String> = emptyList(),
    profilePicturePath: String? = null,
    val age: Int? = null,
    val birthDate: LocalDate? = null,
    val birthPlace: String? = null,
    val permanentAddress: String? = null,
    val className: String? = null,
    val classGroups: String? = null,
    val classNumber: Int? = null,
    val guardians: List<Guardian> = emptyList(),
    val sposaVariableSymbol: String? = null,
    val sposaBankAccount: String? = null,
) : SchoolAttendee(fullName, username, schoolMail, privateMail, phoneNumbers, profilePicturePath)
{

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Student

        if (age != other.age) return false
        if (birthDate != other.birthDate) return false
        if (birthPlace != other.birthPlace) return false
        if (permanentAddress != other.permanentAddress) return false
        if (className != other.className) return false
        if (classGroups != other.classGroups) return false
        if (classNumber != other.classNumber) return false
        if (guardians != other.guardians) return false
        if (sposaVariableSymbol != other.sposaVariableSymbol) return false
        if (sposaBankAccount != other.sposaBankAccount) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = age ?: 0
        result = 31 * result + (birthDate?.hashCode() ?: 0)
        result = 31 * result + (birthPlace?.hashCode() ?: 0)
        result = 31 * result + (permanentAddress?.hashCode() ?: 0)
        result = 31 * result + (className?.hashCode() ?: 0)
        result = 31 * result + (classGroups?.hashCode() ?: 0)
        result = 31 * result + (classNumber ?: 0)
        result = 31 * result + guardians.hashCode()
        result = 31 * result + (sposaVariableSymbol?.hashCode() ?: 0)
        result = 31 * result + (sposaBankAccount?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String
    {
        return "Student(" +
                "fullName='$fullName', " +
                "username='$username', " +
                "schoolMail='$schoolMail', " +
                "privateMail=$privateMail, " +
                "phoneNumbers=$phoneNumbers, " +
                "profilePicturePath=$profilePicturePath, " +
                "age=$age, " +
                "birthDate=$birthDate, " +
                "birthPlace=$birthPlace, " +
                "permanentAddress=$permanentAddress, " +
                "className=$className, " +
                "classGroups=$classGroups, " +
                "classNumber=$classNumber, " +
                "guardians=$guardians, " +
                "sposaVariableSymbol=$sposaVariableSymbol, " +
                "sposaBankAccount=$sposaBankAccount, " +
                ")"
    }
}
