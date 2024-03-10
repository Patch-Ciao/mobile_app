package com.example.libralink

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.*
import com.example.libralink.HistoryAdapter


class History : AppCompatActivity() {

//    private lateinit var historyListView: ListView
//    private lateinit var adapter: ArrayAdapter<String>
//    private lateinit var studentId: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.fragment_history)
//
//        historyListView = findViewById(R.id.historyListView)
//        adapter = ArrayAdapter(this, R.layout.history_list_item)
//        historyListView.adapter = adapter
//
//        // Get studentId from the intent
//        studentId = intent.getStringExtra("studentId") ?: ""
//
//        // Initialize Firebase
//        val db = FirebaseFirestore.getInstance()
//
//        // Replace "history" with the actual name of your Firebase collection
//        // Update the query to filter by studentId
//        db.collection("history")
//            .whereEqualTo("studentId", studentId)
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    // Customize this part based on your Firestore document structure
//                    val bookName = document.getString("bookName") ?: ""
//                    val borrowedDate = document.getString("borrowedDate") ?: ""
//                    val returnDate = document.getString("returnDate") ?: ""
//
//                    val listItem =
//                        "Book Name: $bookName\nBorrowed Date: $borrowedDate\nReturn Date: $returnDate"
//                    adapter.add(listItem)
//                }
//            }
//            .addOnFailureListener { exception ->
//                // Handle errors here
//            }
//    }
}

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

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








//class HistoryFragment : Fragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_history, container, false)
//    }
//
////    override fun onActivityCreated(savedInstanceState: Bundle?) {
////        super.onActivityCreated(savedInstanceState)
////
////        // Find the TextView by its ID
////        val titleTextView: TextView = requireView().findViewById(R.id.textViewTitle)
////
////        // Set the text dynamically for this page
////        titleTextView.text = "Different Text for This Page"
////    }
//}

