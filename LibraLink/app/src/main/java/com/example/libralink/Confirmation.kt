package com.example.libralink

import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.libralink.databinding.FragmentConfirmationBinding
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.integrity.internal.al
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.ParseException
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class Confirmation {
}
class ConfirmationFragment : Fragment() {

    private lateinit var binding: FragmentConfirmationBinding
    private lateinit var database: DatabaseReference
    private lateinit var imageViewBookPic: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference // Replace with Firestore if you're using Firestore
        imageViewBookPic = view.findViewById(R.id.imageViewBookPic)

        // Retrieve the barcodeValue from arguments
        val barcodeValue = arguments?.getString("barcodeValue", "No Value") ?: "No Value"
        val studentId = arguments?.getString("studentId", "No Value") ?: "No Value"


        // Retrieve and display book information based on the barcodeValue
        retrieveBookInfo(barcodeValue)
        binding.btnConfirm.setOnClickListener {
            Log.e("Checkit", "insideconfirm")
            val borrowedDate = getCurrentDate() // Get the current date in "dd/MM/yy" format
            val renew = "2"
            val returnDate = calculateReturnDate(borrowedDate)


            getBookStatusAsync(barcodeValue) { bookStatus ->
                Log.d("Checkit", "Book Status: $bookStatus")
                when (bookStatus) {
                    "Available" -> {
                        Log.e("Checkit", "Available")
                        updateBookStatus(barcodeValue, "Fully booked")

                        // Proceed to add to the "BookTransaction" node
                        val bookTransactionRef = database.child("bookTransactions").push()

                        bookTransactionRef.child("borrowedDate").setValue(borrowedDate)
                        bookTransactionRef.child("copyId").setValue(barcodeValue)
                        bookTransactionRef.child("renew").setValue(renew)
                        bookTransactionRef.child("returnDate").setValue(returnDate)
                        bookTransactionRef.child("studentId").setValue(studentId)

                        // Display a success popup
                        showSuccessPopup("Successfully booked")

                        // Perform confirmation actions if needed
                        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.fragment_container, HomeFragment())
                        fragmentTransaction.addToBackStack(null)
                        fragmentTransaction.commit()
                    }


                    "Fully booked" -> {
                        Log.e("Checkit", "Out of stock")
                        lifecycleScope.launch {
                        if (isTransactionExists(studentId, barcodeValue)) {
                            // Data does not exist, check if the date is within the range
                            if (isDateInRange(borrowedDate, returnDate, getCurrentDate())) {
                                // Date is within the range, check if renew is 0 or not
                                getRenewValue(studentId, barcodeValue) { renewValue ->
                                    Log.e("Checkit", "renewValue123: $renewValue")

                                    if (renewValue == 0) {
                                        // Renew is 0, show a popup message
                                        showPopup("Sorry, you can't renew this book. Renew quota exceeded.")
                                    } else {
                                        Log.e("Checkit", "renewValue2 $renewValue")

                                        // Renew is not 0, decrement renew value by 1 and update it in the "books" node
                                        updateRenewInBookTransaction(barcodeValue, renewValue - 1)
                                        Log.e("Checkit", "renewValue3 $renewValue")

                                        showPopup("Book renewed successfully.")

                                        // Handle other actions as needed
                                        // For example:
                                        // showPopup("Book renewed successfully!")
                                    }
                                }

                            } else {
                                // Date is not within the range, add a new transaction
                                addNewTransaction(
                                    borrowedDate,
                                    returnDate,
                                    renew,
                                    barcodeValue,
                                    studentId
                                )
                            }
                        } else {
                            Log.e("Checkit", "should open")
                            showPopup("Sorry, the book is currently Fully booked.")
                        }
                    }


                }
            }
            }
        }

        // Function to show a popup message (You can replace this with your actual popup implementation)


        binding.btnCancel.setOnClickListener {
            // Perform cancel actions if needed
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, HomeFragment())
            fragmentTransaction.addToBackStack(null) // Optional: add to back stack if you want to navigate back
            fragmentTransaction.commit()

            // Navigate to HomeFragment
            //findNavController().navigate(R.id.action_confirmationFragment_to_homeFragment)
        }
    }
    private fun showSuccessPopup(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Success")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ ->
            // Do something when the "OK" button is clicked, if needed
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun isDateInRange(startDate: String, endDate: String, currentDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yy")

        try {
            val parsedStartDate = dateFormat.parse(startDate)
            val parsedEndDate = dateFormat.parse(endDate)
            val parsedCurrentDate = dateFormat.parse(currentDate)
            Log.e("dateFormat", "EdateFormat: parsedStartDate$parsedStartDate parsedEndDate$parsedEndDate parsedCurrentDate$parsedCurrentDate")

            // Check if the current date is within the range of borrowed and return dates
            return !(parsedCurrentDate.before(parsedStartDate) || parsedCurrentDate.after(parsedEndDate))
        } catch (e: ParseException) {
            Log.e("DateError", "Error parsing dates: ${e.message}")
            return false
        }
    }
    private fun getRenewValue(studentId: String, copyId: String, callback: (Int) -> Unit) {
        val transactionsRef = database.child("bookTransactions")

        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var renewValue = 0

                for (transactionSnapshot in snapshot.children) {
                    val currentStudentId = transactionSnapshot.child("studentId").getValue(String::class.java)
                    val currentCopyId = transactionSnapshot.child("copyId").getValue(String::class.java)
                    Log.e("Checkit", "Renew valuein1: $renewValue")

                    if (currentStudentId == studentId && currentCopyId == copyId) {
                        // Match found, retrieve renew as String and convert it to Int
                        val renewAsString = transactionSnapshot.child("renew").getValue(String::class.java)
                        renewValue = renewAsString?.toIntOrNull() ?: 0
                        Log.e("Checkit", "Renew valuein: $renewValue")

                        break
                    }
                }

                Log.e("Checkit", "Renew value: $renewValue")
                callback(renewValue)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors, if needed
                Log.e("FirebaseError", "Error getting renew value: ${error.message}")
                callback(0) // Return the default value in case of an error
            }
        })
    }




    private fun getBookStatusAsync(copyId: String, callback: (String) -> Unit) {
        val booksRef = database.child("books")

        var status = "Unknown"

        booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (bookSnapshot in snapshot.children) {
                    val copiesSnapshot = bookSnapshot.child("copies")
                    for (copySnapshot in copiesSnapshot.children) {
                        val bookCopyId = copySnapshot.child("copyId").getValue(String::class.java)
                        if (copyId == bookCopyId) {
                            val rawStatus = copySnapshot.child("status").getValue(String::class.java)
                            status = rawStatus ?: "Unknown"
                            Log.d("BookStatus", "Book Status: $status for copyId: $copyId")
                            callback(status)  // Pass the status to the callback
                            return
                        }
                    }
                }
                Log.d("BookStatus", "Book Status: $status lastpart")
                callback(status)  // Pass the status to the callback even if not found
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error getting book status: ${error.message}")
                callback(status)  // Pass the status to the callback in case of error
            }
        })
    }




    private fun showOutOfStockMessage() {
        showPopup("Sorry, the book is currently Fully booked.")
    }

    private fun calculateReturnDate(borrowedDate: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yy")
        val calendar = Calendar.getInstance()

        // Parse the borrowed date
        val parsedBorrowedDate = dateFormat.parse(borrowedDate)
        calendar.time = parsedBorrowedDate

        // Add 14 days to the borrowed date for the return date
        calendar.add(Calendar.DAY_OF_MONTH, 14)

        return dateFormat.format(calendar.time)
    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yy")
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
    private fun showPopup(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun addNewTransaction(borrowedDate: String, returnDate: String, renew: String, barcodeValue: String, studentId: String) {
        val bookTransactionRef = database.child("bookTransactions").push()
        bookTransactionRef.child("borrowedDate").setValue(borrowedDate)
        bookTransactionRef.child("copyId").setValue(barcodeValue)
        bookTransactionRef.child("renew").setValue(renew)
        bookTransactionRef.child("returnDate").setValue(returnDate)
        bookTransactionRef.child("studentId").setValue(studentId)

        // Perform confirmation actions if needed
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, HomeFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun updateRenewInBookTransaction(copyId: String, newRenewValue: Int) {
        val bookTransactionsRef = database.child("bookTransactions")

        bookTransactionsRef.orderByChild("copyId").equalTo(copyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (transactionSnapshot in snapshot.children) {
                        val newRenewStringValue = newRenewValue.toString()
                        transactionSnapshot.ref.child("renew").setValue(newRenewStringValue)
                        Log.e("Checkit", "newRenewValue in bookTransaction: $newRenewValue")
                        break
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error updating renew value in bookTransaction: ${error.message}")
                }
            })
    }



    private fun updateBookStatus(copyId: String, status: String) {
        val booksRef = database.child("books")

        booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (bookSnapshot in snapshot.children) {
                    val copiesSnapshot = bookSnapshot.child("copies")
                    for (copySnapshot in copiesSnapshot.children) {
                        val bookCopyId = copySnapshot.child("copyId").getValue(String::class.java)
                        if (copyId == bookCopyId) {
                            copySnapshot.ref.child("status").setValue(status)
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error updating book status: ${error.message}")
            }
        })
    }

    private suspend fun isTransactionExists(studentId: String, copyId: String): Boolean {
        val transactionsRef = database.child("bookTransactions")

        return suspendCoroutine { continuation ->
            transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var transactionExists = false

                    for (transactionSnapshot in snapshot.children) {
                        val currentStudentId = transactionSnapshot.child("studentId").getValue(String::class.java)
                        val currentCopyId = transactionSnapshot.child("copyId").getValue(String::class.java)

                        if (currentStudentId == studentId && currentCopyId == copyId) {
                            transactionExists = true
                            break
                        }
                    }

                    // Pass the result to the continuation
                    continuation.resume(transactionExists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error checking if transaction exists: ${error.message}")
                    // Pass the result to the continuation with transactionExists as false
                    continuation.resume(false)
                }
            })
        }
    }




    private fun retrieveBookInfo(barcodeValue: String) {
        // Assume you have a "books" node in your Firebase database
        val booksRef = database.child("books")

        booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Iterate through the books
                for (bookSnapshot in snapshot.children) {
                    val copiesSnapshot = bookSnapshot.child("copies")
                    // Iterate through the copies of each book
                    for (copySnapshot in copiesSnapshot.children) {
                        val bookCopyId = copySnapshot.child("copyId").getValue(String::class.java)
                        if (barcodeValue == bookCopyId) {
                            // Matching copyId found, retrieve book information
                            val matchedTitle = bookSnapshot.child("title").getValue(String::class.java)
                            val matchedAuthor = bookSnapshot.child("author").getValue(String::class.java)
                            val matchedPic = bookSnapshot.child("pic").getValue(String::class.java)
                            val matchedStatus = copySnapshot.child("status").getValue(String::class.java)

                            // Log the data to check if it's retrieved correctly
                            Log.d("BookInfo", "Title: $matchedTitle, Author: $matchedAuthor, Status: $matchedStatus")

                            // Construct the image file name based on the pic field
                            val imageName = "$matchedPic" // Adjust the file extension based on your actual file format

                            // Assuming you have an ImageView named "imageViewBookPic" in your layout
                            // Load the image using an appropriate method (e.g., Glide, Picasso)
                            // Assuming you have an ImageView named "imageViewBookPic" in your layout
// Load the image using an appropriate method (e.g., Glide, Picasso)
                            val resourceId = resources.getIdentifier(imageName, "drawable", requireActivity().packageName)
                            Log.e("BookInfo", "Image  resourceId$resourceId")

                            if (resourceId != 0) {
                                // Display the image if it exists
                                imageViewBookPic.setImageResource(resourceId)
                            } else {
                                // Set a placeholder image when the desired image doesn't exist
                                imageViewBookPic.setImageResource(R.drawable.booknotfound)
                                // Log the error message
                                Log.e("BookInfo", "Image not found for $imageName")
                            }



                            // Display the book information in the UI
                            val bookInfo = "Book Title: $matchedTitle\nAuthor: $matchedAuthor\nStatus: $matchedStatus"
                            binding.textViewBookInfo.text = bookInfo

                            return // Exit the loop once a match is found
                        }
                    }
                }
                // Handle case when no matching copyId is found for the barcodeValue
                binding.textViewBookInfo.text = "No matching book found for the given barcode."
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                Log.e("FirebaseError", "Error retrieving data: ${error.message}")
            }
        })
    }


}
