package com.example.libralink

import com.google.firebase.database.DatabaseReference
import com.github.javafaker.Faker
import kotlin.random.Random

class UserDataGenerator(private val database: DatabaseReference, private val faker: Faker) {

    fun generateAndWriteFakeUserData(count: Int) {
        repeat(count) {
            val year = Random.nextInt(1, 5)
            val studentId = generateStudentId(year)
            val name = faker.name().firstName()
            val surname = faker.name().lastName()
            val password = faker.internet().password()
            val faculty = generateFaculty()
            val major = generateMajor(faculty)
            val email = generateEmail(name, surname, year)

            // Create User object
            val user = User(studentId, name, surname, password, year, faculty, major, email)

            // Write fake user data to the database
            writeNewUser(user)
        }
    }

    private fun generateStudentId(year: Int): String {
        val yearPrefix = when (year) {
            1 -> "23"
            2 -> "22"
            3 -> "21"
            4 -> "20"
            else -> "20" // Default to year 4 if not specified
        }
        val randomDigits = (1..(10 - yearPrefix.length)).joinToString("") { Random.nextInt(0, 9).toString() }
        return "$yearPrefix$randomDigits"
    }

    private fun generateFaculty(): String {
        val faculties = listOf("TNIC", "Engineering", "Business Administration", "Information Technology")
        return faculties.random()
    }

    private fun generateMajor(faculty: String): String {
        return when (faculty) {
            "TNIC" -> listOf("DSA", "DGE", "IBN").random()
            "Engineering" -> listOf("AE", "RE", "EE", "IE", "CE").random()
            "Business Administration" -> listOf("DBS", "BJ", "IB", "AC", "HRJ", "LM", "DM", "TH").random()
            "Information Technology" -> listOf("IT", "BI", "MT", "DC").random()
            else -> ""
        }
    }

    private fun generateEmail(name: String, surname: String, year: Int): String {
        val lastNamePrefix = surname.take(2)
        return "${lastNamePrefix.toLowerCase()}.${name.toLowerCase()}_st@tni.ac.th"
    }



    private fun writeNewUser(user: User) {
        val nonNullableStudentId = user.studentId ?: throw IllegalArgumentException("Student ID cannot be null")
        database.child("users").child(nonNullableStudentId).setValue(user)
    }
}
