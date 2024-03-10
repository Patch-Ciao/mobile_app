package com.example.libralink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*

class HomePageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page1 -> {
                    val studentId = intent.getStringExtra("studentId")

                    replaceFragment(HomeFragment().apply {
                        arguments = Bundle().apply {
                            putString("studentId", studentId)

                        }
                    })
                    return@OnNavigationItemSelectedListener true
                }
                R.id.page2 -> {
                    val studentId = intent.getStringExtra("studentId")

                    replaceFragment(ScanFragment().apply {
                        arguments = Bundle().apply {
                            putString("studentId", studentId)

                        }
                    })
                    return@OnNavigationItemSelectedListener true
                }
                R.id.page3 -> {
                    replaceFragment(SearchFragment())
                    return@OnNavigationItemSelectedListener true
                }


                R.id.page4 -> {
                    val studentId = intent.getStringExtra("studentId")

                    replaceFragment(HistoryFragment().apply {
                        arguments = Bundle().apply {
                            putString("studentId", studentId)

                        }
                    })
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Load the initial fragment only if the fragment container is empty
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(HomeFragment())
        }
        val name = intent.getStringExtra("name")
        val surname = intent.getStringExtra("surname")

        // Concatenate and display the user's name and surname
        val userNameTextView: TextView = findViewById(R.id.textViewUser)
        userNameTextView.text = "$name $surname"

        val signOutImageView: ImageView = findViewById(R.id.imageViewSignOut)

        signOutImageView.setOnClickListener {
            signOut()
        }

    }





    private fun signOut() {
        // Create a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Sign Out")
        builder.setMessage("Are you sure you want to sign out?")

        builder.setPositiveButton("Yes") { dialog, which ->
            // User clicked Yes, sign out the user
            auth.signOut()

            // Redirect to the login page
            val intent = Intent(this@HomePageActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optional: finish the current activity if you don't want to go back to it
        }

        builder.setNegativeButton("No") { dialog, which ->
            // User clicked No, do nothing
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        // Hide the TextView when a fragment is loaded
        findViewById<TextView>(R.id.textViewLogin)?.visibility = View.GONE
    }
}


// Import statements

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Retrieve studentId from arguments
        val studentId = arguments?.getString("studentId")

        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        historyAdapter = HistoryAdapter(emptyList())
        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val noHistoryTextView: TextView = view.findViewById(R.id.noHistoryTextView)

        val databaseReference = FirebaseDatabase.getInstance().getReference("bookTransactions")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val transactions = mutableListOf<BookTransaction>()
                for (snapshot in dataSnapshot.children) {
                    val transaction = snapshot.getValue(BookTransaction::class.java)
                    transaction?.let { transactions.add(it) }
                }

                // Filter transactions based on studentId
                val filteredTransactions = transactions.filter { it.studentId == studentId }

                if (filteredTransactions.isEmpty()) {
                    // No transactions for the student
                    noHistoryTextView.visibility = View.VISIBLE
                    return  // Exit the function early
                } else {
                    noHistoryTextView.visibility = View.GONE
                }

                fetchBookTitles(filteredTransactions)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        return view
    }

    private fun fetchBookTitles(transactions: List<BookTransaction>) {
        val booksReference = FirebaseDatabase.getInstance().getReference("books")
        booksReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(booksSnapshot: DataSnapshot) {
                val transactionsWithTitles = mutableListOf<BookTransaction>()
                val matchedTitles = mutableMapOf<String, String>()
                val matchedPics = mutableMapOf<String, String>()

                for (transaction in transactions) {
                    val copyId = transaction.copyId
                    val title = getTitleFromBooksNode(booksSnapshot, copyId)
                    val book = getBookFromBooksNode(booksSnapshot, copyId)

                    transactionsWithTitles.add(transaction.copy(title = title, pic = book?.pic ?: ""))
                    Log.d("FetchBookTitles", "transactionsWithTitles $transactionsWithTitles")

                    // Check if copyId matches with bookCopyId and store the matched title and pic
                    for (bookSnapshot in booksSnapshot.children) {
                        val copiesSnapshot = bookSnapshot.child("copies")
                        for (copySnapshot in copiesSnapshot.children) {
                            val bookCopyId = copySnapshot.child("copyId").getValue(String::class.java)
                            if (copyId == bookCopyId) {
                                val matchedTitle = bookSnapshot.child("title").getValue(String::class.java)
                                val matchedPic = bookSnapshot.child("pic").getValue(String::class.java)
                                matchedTitles[copyId] = matchedTitle ?: "Title not found"
                                matchedPics[copyId] = matchedPic ?: "DefaultPic"
                                Log.d("FetchBookTitles", "transactionsWithTitles $transactionsWithTitles")

                            }
                        }
                    }
                }

                // Check if there are no transactions with titles
                if (transactionsWithTitles.isEmpty()) {
                    val placeholderMessage = "No history available"
                    historyAdapter.updateData(emptyList(), emptyMap(), emptyMap(), placeholderMessage)
                } else {
                    // Update the adapter with the fetched data
                    historyAdapter.updateData(transactionsWithTitles, matchedTitles, matchedPics, "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the case when there is an error during the database operation
                val errorMessage = "Error fetching data: ${error.message}"
                historyAdapter.updateData(emptyList(), emptyMap(), emptyMap(), errorMessage)
            }
        })
    }



    private fun getBookFromBooksNode(booksSnapshot: DataSnapshot, copyId: String): Book? {
        for (bookSnapshot in booksSnapshot.children) {
            val copiesSnapshot = bookSnapshot.child("copies")
            for (copySnapshot in copiesSnapshot.children) {
                val bookCopyId = copySnapshot.child("copyId").getValue(String::class.java)
                if (copyId == bookCopyId) {
                    return bookSnapshot.getValue(Book::class.java)
                }
            }
        }
        return null
    }




    private fun getTitleFromBooksNode(booksSnapshot: DataSnapshot, copyId: String): String {
        for (bookSnapshot in booksSnapshot.children) {
            val copiesSnapshot = bookSnapshot.child("copies")
            if (copiesSnapshot.hasChild(copyId)) {
                return bookSnapshot.child("title").getValue(String::class.java) ?: ""
            }
        }
        return ""
    }
}

