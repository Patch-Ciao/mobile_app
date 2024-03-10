package com.example.libralink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity





class AdvancedSearchActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextAuthor: EditText
    private lateinit var editTextISBN: EditText
    private lateinit var editTextKeyword: EditText
    private lateinit var editTextBarcode: EditText
    private lateinit var buttonSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advance_search)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextAuthor = findViewById(R.id.editTextAuthor)
        editTextISBN = findViewById(R.id.editTextISBN)
        editTextKeyword = findViewById(R.id.editTextKeyword)
        editTextBarcode = findViewById(R.id.editTextBarcode)
        buttonSearch = findViewById(R.id.buttonSearch)

        // Set onClickListener for the search button
        buttonSearch.setOnClickListener {
            // Get search queries from EditText fields
            val titleQuery = editTextTitle.text.toString().trim()
            val authorQuery = editTextAuthor.text.toString().trim()
            val isbnQuery = editTextISBN.text.toString().trim()
            val keywordQuery = editTextKeyword.text.toString().trim()
            val barcodeQuery = editTextBarcode.text.toString().trim()

            // Create Intent for SearchResultActivity
            val intent = Intent(this, SearchResultActivity::class.java).apply {
                // Put search queries as extras
                putExtra("title_query", titleQuery)
                putExtra("author_query", authorQuery)
                putExtra("isbn_query", isbnQuery)
                putExtra("keyword_query", keywordQuery)
                putExtra("barcode_query", barcodeQuery)
            }

            // Start SearchResultActivity
            startActivity(intent)
        }
    }
}
