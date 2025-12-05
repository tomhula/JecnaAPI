package me.tomasan7.jecnaapi.util

import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.JecnaClient
import org.junit.Test

internal class ProfilePageTest {
    @Test
    fun parseProfilePage(){
        val client = JecnaClient()
        runBlocking {
            client.login("vegh","Fkcw5428")
            val student = client.getStudentProfile()
            println(student.toString())
        }
    }

}