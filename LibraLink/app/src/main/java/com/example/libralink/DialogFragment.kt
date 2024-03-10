package com.example.libralink
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.libralink.databinding.FragmentBookDetailsDialogBinding
import com.google.firebase.database.*

class BookDetailsDialogFragmentCustom : DialogFragment() {

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
        val view = inflater.inflate(R.layout.dialog_book_details, container, false)

        // Initialize views
        textViewTitle = view.findViewById(R.id.textViewTitle)
        textViewDescription = view.findViewById(R.id.textViewDescription)
        textViewISBN = view.findViewById(R.id.textViewISBN)
        textViewLanguage = view.findViewById(R.id.textViewLanguage)
        textViewPublicationYear = view.findViewById(R.id.textViewPublicationYear)
        textViewPublisher = view.findViewById(R.id.textViewPublisher)
        textViewType = view.findViewById(R.id.textViewType)
        textViewCopies = view.findViewById(R.id.textViewCopies)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("books")

        // Retrieve book ID from arguments
        val bookId = arguments?.getString("bookId")

        // Fetch book details from Firebase
        bookId?.let { fetchBookDetails(it) }

        return view
    }

    private fun fetchBookDetails(bookId: String) {
        databaseReference.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val book = dataSnapshot.getValue(Book::class.java)

                // Set book details to TextViews
                textViewTitle.text = "Title: ${book?.title}"
                textViewDescription.text = "Description: ${book?.description}"
                textViewISBN.text = "ISBN: ${book?.isbn}"
                textViewLanguage.text = "Language: ${book?.language}"
                textViewPublicationYear.text = "Publication Year: ${book?.publicationYear}"
                textViewPublisher.text = "Publisher: ${book?.publisher}"
                textViewType.text = "Type: ${book?.type}"
                textViewCopies.text = "Copies: ${book?.copies}"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}
