package com.example.libralink
// BookDetailsDialogFragment.kt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.libralink.databinding.FragmentBookDetailsDialogBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookDetailsDialogFragment : DialogFragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var textViewISBN: TextView
    private lateinit var textViewLanguage: TextView
    private lateinit var textViewPublicationYear: TextView
    private lateinit var textViewPublisher: TextView
    private lateinit var textViewType: TextView
    private lateinit var textViewCopies: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: FragmentBookDetailsDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_book_details, container, false)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("books")

        // Retrieve book ID from arguments
        val bookId = arguments?.getString("bookId")

        // Fetch book details from Firebase
        bookId?.let { fetchBookDetails(it) }

        return binding.root
    }

    private fun fetchBookDetails(bookId: String) {
        databaseReference.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val book = dataSnapshot.getValue(Book::class.java)

                // Set book details to TextViews using data binding
                binding.book = book
                binding.executePendingBindings()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

}
