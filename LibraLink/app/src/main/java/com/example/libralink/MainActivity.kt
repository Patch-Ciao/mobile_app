package com.example.libralink

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.libralink.ui.theme.LibraLinkTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.github.javafaker.Faker
import com.google.firebase.FirebaseApp
import kotlin.random.Random
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.libralink.LoginActivity
import com.example.libralink.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth

// Create an instance of UserDataGenerator
//val userDataGenerator = UserDataGenerator(database, faker)
// Generate and write fake user data using the UserDataGenerator
//userDataGenerator.generateAndWriteFakeUserData(10)

// Create an instance of BookDataGenerator
//val bookDataGenerator = BookDataGenerator(database, faker)

// Generate and write fake book data using the BookDataGenerator
//bookDataGenerator.generateAndWriteFakeBooks(10)

// Create an instance of DataDeleter
//val dataDeleter = DataDeleter(database)
// Delete all users and books using the DataDeleter instance
//dataDeleter.deleteAllUsers()
//dataDeleter.deleteAllBooks()

class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private val faker = Faker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase database reference
        database = Firebase.database.reference
        val user = Firebase.auth.currentUser


        // Sample data for BookTransaction
        val bookTransaction = BookTransaction(
            studentId = "2026453271",
            copyId = "4076214637",
            borrowedDate = "12/01/2024",
            returnDate = "26/01/2024",
            renew = "1"
        )

        // Push data to Firebase Realtime Database under "bookTransactions" node
        pushBookTransactionToFirebase(bookTransaction)
    }

    private fun pushBookTransactionToFirebase(bookTransaction: BookTransaction) {
        // Reference to the "bookTransactions" node
        val bookTransactionsRef = database.child("bookTransactions")

        // Push the data to the "bookTransactions" node
        val transactionReference = bookTransactionsRef.push()
        transactionReference.setValue(bookTransaction)
            .addOnSuccessListener {
                // Data successfully pushed to Firebase
                println("Data pushed successfully!")
            }
            .addOnFailureListener {
                // Handle the failure
                println("Error pushing data to Firebase: ${it.message}")
            }
    }
}

//data class BookTransaction(
//    val studentId: String,
//    val copyId: String,
//    val borrowedDate: String,
//    val returnDate: String,
//    val renew: String
//)