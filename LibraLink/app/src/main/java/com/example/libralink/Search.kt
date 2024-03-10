package com.example.libralink

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
//import com.bumptech.glide.Glide

class SearchFragment : Fragment() {

    private lateinit var editTextBookName: EditText
    private lateinit var buttonSearch: Button
    private lateinit var buttonAdvanceSearch: Button
    private lateinit var textViewSearchResult: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imageViewBook: ImageView
    private lateinit var resultLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize views
        editTextBookName = view.findViewById(R.id.editTextBookName)
        buttonSearch = view.findViewById(R.id.buttonSearch)
        buttonAdvanceSearch = view.findViewById(R.id.buttonAdvanceSearch)
        textViewSearchResult = view.findViewById(R.id.textViewSearchResult)
        imageViewBook = view.findViewById(R.id.imageViewResult)
        resultLayout = view.findViewById(R.id.resultLayout)

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("books")

        // Set onClickListener for the search button
        buttonSearch.setOnClickListener {
            searchBook()
        }

        // Set onClickListener for the advance search button
        buttonAdvanceSearch.setOnClickListener {
            val intent = Intent(activity, AdvancedSearchActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun searchBook() {
        val keyword = editTextBookName.text.toString().toLowerCase()
        val databaseReference = FirebaseDatabase.getInstance().reference.child("books")
        if (keyword.isEmpty()) {
            textViewSearchResult.text = "กรุณากรอกคำค้นหา"
            return
        }
        resultLayout.removeAllViews()

        databaseReference.orderByChild("title").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0
                for (bookSnapshot in dataSnapshot.children) {
                    val book = bookSnapshot.getValue(Book::class.java)
                    val title = book?.title?.toLowerCase()
                    val picName = book?.pic
                    if (title != null && title.contains(keyword)) {
                        val resultItemLayout = RelativeLayout(requireContext())
                        resultItemLayout.id = View.generateViewId()
                        resultItemLayout.setOnClickListener {
                            showBookDetailsDialog(bookSnapshot.key.toString())
                        }

                        val layoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        )

                        if (count == 0) {
                            layoutParams.addRule(RelativeLayout.BELOW, R.id.buttonSearch)
                        } else {
                            layoutParams.addRule(RelativeLayout.BELOW, resultLayout.getChildAt(count - 1).id)
                        }
                        resultItemLayout.layoutParams = layoutParams

                        val imageView = ImageView(requireContext())
                        val resourceId = resources.getIdentifier(picName, "drawable", requireActivity().packageName)
                        if (resourceId != 0) {
                            imageView.setImageResource(resourceId)
                            imageView.id = View.generateViewId()
                            imageView.visibility = View.VISIBLE
                            val imageLayoutParams = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            )
                            imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
                            imageLayoutParams.setMargins(50, 0, 16, 50)
                            imageView.layoutParams = imageLayoutParams
                            resultItemLayout.addView(imageView)
                        }

                        val textView = TextView(requireContext())
                        textView.text = "Title: ${book?.title}\nAuthor: ${book?.author}\nISBN: ${book?.isbn}\nKeywords: ${book?.keyword}\nBarcode: ${book?.barcode}"
                        val textLayoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        )
                        textLayoutParams.addRule(RelativeLayout.END_OF, imageView.id)
                        textLayoutParams.setMargins(50, 0, 0, 50)
                        textView.layoutParams = textLayoutParams
                        resultItemLayout.addView(textView)

                        resultLayout.addView(resultItemLayout)
                        count++
                    }
                }
                if (resultLayout.childCount == 0) {
                    textViewSearchResult.text = "No matching books found."
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                textViewSearchResult.text = "A search error occurred."
            }
        })
    }
    private fun showBookDetailsDialog(bookId: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("books").child(bookId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val book = dataSnapshot.getValue(Book::class.java)
                val picName = book?.pic

                // ดึง resourceId ของรูปภาพจากชื่อรูปภาพที่ได้จาก Firebase Realtime Database
                val resourceId = resources.getIdentifier(picName, "drawable", requireActivity().packageName)

                // สร้าง ImageView เพื่อแสดงรูปภาพใน pop-up
                val imageView = ImageView(requireContext())
                imageView.setImageResource(resourceId)

                // กำหนด margin ของ ImageView ด้านบนและด้านล่าง
                val imageLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL  // กำหนดให้รูปภาพอยู่กึ่งกลางซ้ายขวา

                imageLayoutParams.setMargins(50, 50, 50, 50)
                imageView.layoutParams = imageLayoutParams

                // สร้าง TextView เพื่อแสดงข้อมูลจาก Firebase
                val textView = TextView(requireContext())
                val copies = book?.copies

                val stringBuilder = StringBuilder()
                copies?.forEachIndexed { index, copy ->
                    copy?.let {
                        stringBuilder.append("Book ${index + 1}:\n")
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

                // กำหนด margin ของ TextView ด้านซ้ายและด้านขวา
                val textLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textLayoutParams.setMargins(50, 0, 50, 0)
                textView.layoutParams = textLayoutParams

                // สร้าง Layout สำหรับรูปภาพและข้อมูล
                val layout = LinearLayout(requireContext())
                layout.orientation = LinearLayout.VERTICAL
                layout.addView(imageView)
                layout.addView(textView)

                // สร้าง AlertDialog และแสดง pop-up
                AlertDialog.Builder(requireContext())
                    .setView(layout)
                    .show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }}




