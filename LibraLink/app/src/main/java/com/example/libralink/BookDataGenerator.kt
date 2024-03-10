package com.example.libralink

import com.google.firebase.database.DatabaseReference
import com.github.javafaker.Faker
import kotlin.random.Random


class BookDataGenerator(private val database: DatabaseReference, private val faker: Faker) {

    fun generateAndWriteFakeBooks(count: Int) {
        repeat(count) {
            val bookId = generateBookId()
            val title = faker.book().title()
            val author = faker.book().author()
            val isbn = generateIsbn()
            val keyword = faker.lorem().word()
            val barcode = generateBarcode()
            val type = generateBookType()
            val numberOfCopies = Random.nextInt(1, 5) // Random number of copies

            val copies = generateBookCopies(numberOfCopies)

            val book = Book(
                bookId,
                title,
                author,
                isbn,
                keyword,
                barcode,
                type,
                publicationYear = Random.nextInt(1900, 2023),
                publisher = faker.book().publisher(),
                language = faker.nation().language(),
                description = faker.lorem().paragraph(),
                numberOfCopies = numberOfCopies,
                copies = copies
            )

            writeNewBook(book)
        }
    }

    private fun generateBookCopies(numberOfCopies: Int): List<BookCopy> {
        val copies = mutableListOf<BookCopy>()
        repeat(numberOfCopies) {
            val copyId = generateCopyId()
            val status = generateCopyStatus()
            copies.add(BookCopy(copyId, status))
        }
        return copies
    }

    private fun generateCopyId(): String {
        return Random.nextLong(1_000_000_000L, 9_999_999_999L).toString()
    }

    private fun generateCopyStatus(): String {
        return if (Random.nextBoolean()) "Available" else "Fully booked"
    }

    private fun writeNewBook(book: Book) {
        // Use bookId as the key
        database.child("books").child(book.bookId ?: "").setValue(book)
    }

    private fun generateBookId(): String {
        return Random.nextLong(1_000_000_000L, 9_999_999_999L).toString()
    }

    private fun generateIsbn(): String {
        return faker.code().isbn13()
    }

    private fun generateBarcode(): String {
        return faker.code().ean13()
    }

    private fun generateBookType(): String {
        val types = listOf("Fiction", "Non-Fiction", "Thriller", "Romance", "Mystery", "Science Fiction")
        return types.random()
    }

}