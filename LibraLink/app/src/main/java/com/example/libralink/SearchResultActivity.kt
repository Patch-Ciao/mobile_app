package com.example.libralink

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class SearchResultActivity : AppCompatActivity() {

    private lateinit var resultLayout: LinearLayout
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        resultLayout = findViewById(R.id.resultLayout)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("books")

        // Retrieve search queries from intent extras
        val titleQuery = intent.getStringExtra("title_query")
        val authorQuery = intent.getStringExtra("author_query")
        val isbnQuery = intent.getStringExtra("isbn_query")
        val keywordQuery = intent.getStringExtra("keyword_query")
        val barcodeQuery = intent.getStringExtra("barcode_query")

        // Perform search based on the provided queries
        searchBooks(titleQuery, authorQuery, isbnQuery, keywordQuery, barcodeQuery)
    }

    private fun searchBooks(
        titleQuery: String?,
        authorQuery: String?,
        isbnQuery: String?,
        keywordQuery: String?,
        barcodeQuery: String?
    ) {
        var query: Query = databaseReference

        if (!titleQuery.isNullOrEmpty()) {
            val titleKeywords = titleQuery.split(" ")
            // Create a list to hold individual queries for each keyword
            val titleQueries = mutableListOf<Query>()

            // For each keyword, create a query to search for titles
            for (keyword in titleKeywords) {
                val keywordQuery = databaseReference
                    .orderByChild("title")
                    .startAt(keyword)
                    .endAt(keyword + "\uf8ff")
                titleQueries.add(keywordQuery)
            }

            // Combine all individual queries into one compound query using the OR operator
            query = combineQueriesWithOr(titleQueries)
        } else if (!authorQuery.isNullOrEmpty()) {
            // Search for authors that contain the query text
            query = query.orderByChild("author").startAt(authorQuery).endAt(authorQuery + "\uf8ff")
        } else if (!isbnQuery.isNullOrEmpty()) {
            query = query.orderByChild("isbn").equalTo(isbnQuery)
        } else if (!barcodeQuery.isNullOrEmpty()) {
            query = query.orderByChild("barcode").equalTo(barcodeQuery)
        } else if (!keywordQuery.isNullOrEmpty()) {
            // Split all the keywords entered by the user
            val keywords = keywordQuery.split(" ")

            // Create a composite string for searching
            val compositeQuery = keywords.joinToString("\uf8ff") { "\uf8ff$it\uf8ff" }

            // Search using the composite string
            query = query.orderByChild("keyword").startAt(compositeQuery).endAt(compositeQuery + "\uf8ff")
        } else {
            addSearchResult("No search query provided.")
            return
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0
                val resultBooks = mutableListOf<Book>() // List to store matching books
                for (bookSnapshot in dataSnapshot.children) {
                    val book = bookSnapshot.getValue(Book::class.java)
                    if (isBookMatchingSearch(book, titleQuery, authorQuery, isbnQuery, keywordQuery, barcodeQuery)) {
                        resultBooks.add(book!!) // Add the book to the result list
                        count++
                    }
                }
                if (count > 0) {
                    displaySearchResults(resultBooks) // Display the search results
                } else {
                    addSearchResult("No matching books found.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                addSearchResult("Database error: ${databaseError.message}")
            }
        })
    }

    private fun isBookMatchingSearch(
        book: Book?,
        titleQuery: String?,
        authorQuery: String?,
        isbnQuery: String?,
        keywordQuery: String?,
        barcodeQuery: String?
    ): Boolean {
        // Check if any of the book attributes contain the search query
        return (titleQuery.isNullOrEmpty() || book?.title?.contains(titleQuery!!, ignoreCase = true) == true) &&
                (authorQuery.isNullOrEmpty() || book?.author?.contains(authorQuery!!, ignoreCase = true) == true) &&
                (isbnQuery.isNullOrEmpty() || book?.isbn?.contains(isbnQuery!!, ignoreCase = true) == true) &&
                (barcodeQuery.isNullOrEmpty() || book?.barcode?.contains(barcodeQuery!!, ignoreCase = true) == true) &&
                (keywordQuery.isNullOrEmpty() || book?.keyword?.contains(keywordQuery!!, ignoreCase = true) == true)
    }

    private fun displaySearchResults(resultBooks: List<Book>) {
        resultLayout.removeAllViews() // Clear the existing search results

        for (book in resultBooks) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL

            val imageView = ImageView(this)
            val resourceId = resources.getIdentifier(book.pic ?: "", "drawable", packageName)
            if (resourceId != 0) {
                imageView.setImageResource(resourceId)
                imageView.id = View.generateViewId()
                imageView.visibility = View.VISIBLE

                val imageLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageLayoutParams.setMargins(50, 50, 16, 0) // Left margin of the image
                imageView.layoutParams = imageLayoutParams
                imageView.setOnClickListener {
                    showBookDetailsDialog(book.bookId ?: "")
                }
                linearLayout.addView(imageView) // Add ImageView to LinearLayout
            }

            val textView = TextView(this)
            textView.text =
                "Title: ${book.title}\nAuthor: ${book.author}\nISBN: ${book.isbn}\nKeywords: ${book.keyword}\nBarcode: ${book.barcode}\n\n"
            val textLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textLayoutParams.setMargins(16, 50, 50, 0) // Right margin of the text
            textView.layoutParams = textLayoutParams
            linearLayout.addView(textView)

            resultLayout.addView(linearLayout)
        }
    }

    private fun addSearchResult(message: String) {
        val textView = TextView(this)
        textView.text = message
        resultLayout.addView(textView)
    }

    private fun showBookDetailsDialog(bookId: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("books").child(bookId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val book = dataSnapshot.getValue(Book::class.java)
                val picName = book?.pic

                // Get the resource ID of the image from the image name obtained from Firebase Realtime Database
                val resourceId = resources.getIdentifier(picName, "drawable", packageName)

                // Create an ImageView to display the image in the popup
                val imageView = ImageView(this@SearchResultActivity)
                imageView.setImageResource(resourceId)

                // Set margin for the ImageView (top and bottom)
                val imageLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL  // Set the image to be horizontally centered
                imageLayoutParams.setMargins(50, 50, 50, 50)
                imageView.layoutParams = imageLayoutParams

                // Create a TextView to display data from Firebase
                val textView = TextView(this@SearchResultActivity)
                val copies = book?.copies

                val stringBuilder = StringBuilder()
                copies?.forEachIndexed { index, copy ->
                    copy?.let {
                        stringBuilder.append("Copy ${index + 1}:\n")
                        stringBuilder.append("  Status: ${copy.status}\n")
                    }
                }
                val copiesText = stringBuilder.toString()

                textView.text = "Title: ${book?.title}\n" +
                        "Author: ${book?.author}\n" +
                        "Barcode: ${book?.barcode}\n" +
                        "Book ID: $bookId\n" +
                        "ISBN: ${book?.isbn}\n" +
                        "Description: ${book?.description}\n" +
                        "Publication Year: ${book?.publicationYear}\n" +
                        "Number of Books: ${book?.numberOfCopies}\n" +
                        "Book No.:\n$copiesText"

                // Set margin for the TextView (left and right)
                val textLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textLayoutParams.setMargins(50, 0, 50, 0)
                textView.layoutParams = textLayoutParams

                // Create a layout for the image and data
                val layout = LinearLayout(this@SearchResultActivity)
                layout.orientation = LinearLayout.VERTICAL
                layout.addView(imageView)
                layout.addView(textView)

                // Create and display the AlertDialog popup
                AlertDialog.Builder(this@SearchResultActivity)
                    .setView(layout)
                    .show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun combineQueriesWithOr(queries: List<Query>): DatabaseReference {
        // Create a list to hold all the keys
        val keyList = mutableListOf<String>()

        // Add the key of each query to the list
        for (query in queries) {
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        // Add the key of each child snapshot to the list
                        keyList.add(snapshot.key ?: "")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        }

        // Create a new query using the keys in the list
        val databaseReference = FirebaseDatabase.getInstance().reference.child("books")
        for (key in keyList) {
            databaseReference.orderByChild("key").equalTo(key)
        }
        return databaseReference
    }

}
