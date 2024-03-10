package com.example.libralink

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Book(
    val bookId: String? = null,
    val title: String? = null,
    val author: String? = null,
    val isbn: String? = null,
    val keyword: String? = null,
    val barcode: String? = null,
    val type: String? = null,
    val publicationYear: Int? = null,
    val publisher: String? = null,
    val language: String? = null,
    val description: String? = null,
    val numberOfCopies: Int? = null,
    val copies: List<BookCopy>? = null,
    val pic: String? = null

) {
}

@IgnoreExtraProperties
data class BookCopy(
    val copyId: String? = null,
    val status: String? = null
) {
}
