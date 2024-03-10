package com.example.libralink

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val studentId: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val password: String? = null,
    val year: Int? = null,
    val faculty: String? = null,
    val major: String? = null,
    val email: String? = null
) {
    // You can add any additional functions or methods here if needed
}
