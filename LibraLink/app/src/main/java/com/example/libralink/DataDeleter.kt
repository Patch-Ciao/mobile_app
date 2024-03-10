package com.example.libralink

import android.util.Log
import com.google.firebase.database.DatabaseReference

class DataDeleter(private val database: DatabaseReference) {

    fun deleteAllUsers() {
        // Deletes all records under the "users" node
        database.child("users").removeValue()
    }

    fun deleteAllBooks() {
        // Deletes all records under the "books" node
        val booksReference = database.child("books")

        // Remove all values under the "books" node
        booksReference.removeValue()
            .addOnSuccessListener {
                // Handle success
                Log.d("Firebase", "All books deleted successfully.")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("Firebase", "Error deleting books", e)
            }
    }
}
