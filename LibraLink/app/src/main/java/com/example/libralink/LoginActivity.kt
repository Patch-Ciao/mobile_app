package com.example.libralink


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.javafaker.Faker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val databaseReference = database.reference


    private lateinit var auth: FirebaseAuth
    private lateinit var usersReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Database references
        auth = FirebaseAuth.getInstance()
        usersReference = FirebaseDatabase.getInstance().reference.child("users")

        val studentIdEditText: EditText = findViewById(R.id.editTextStudentId)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)

        loginButton.setOnClickListener {
            val studentId = studentIdEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (studentId.isNotEmpty() && password.isNotEmpty()) {
                loginUser(studentId, password)
            } else {
                Toast.makeText(this, "Please enter both student ID and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(studentId: String, password: String) {
        // Authenticate using custom logic
        customSignIn(studentId, password)
    }

    private fun customSignIn(studentId: String, password: String) {
        // Use a Firebase reference to check if the user exists with the provided student ID
        val userReference = usersReference.child(studentId)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val storedPassword = dataSnapshot.child("password").getValue(String::class.java)

                    if (password == storedPassword) {
                        // Passwords match, login successful

                        // Retrieve additional user information
                        val name = dataSnapshot.child("name").getValue(String::class.java)
                        val surname = dataSnapshot.child("surname").getValue(String::class.java)
                        val studentId = dataSnapshot.child("studentId").getValue(String::class.java)

                        // Create an Intent to navigate to HomePageActivity
                        val intent = Intent(this@LoginActivity, HomePageActivity::class.java)

                        // Pass user information to the new activity
                        intent.putExtra("name", name)
                        intent.putExtra("surname", surname)
                        intent.putExtra("studentId", studentId)
                        // Start the new activity
                        startActivity(intent)
                        finish() // Optional: finish the current activity if you don't want to go back to it

                    } else {
                        // Passwords do not match, login failed
                        Log.w("LoginActivity", "Custom sign-in: failure - Incorrect password")
                        Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User with the provided student ID does not exist
                    Log.w("LoginActivity", "Custom sign-in: failure - User not found")
                    Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("LoginActivity", "Custom sign-in: database error", databaseError.toException())
                Toast.makeText(this@LoginActivity, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }







}
