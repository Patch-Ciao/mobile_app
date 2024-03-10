//package com.example.libralink
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//class HistoryAdapter(
//    private var bookTransactions: List<BookTransaction>,
//    private var matchedTitles: Map<String, String> = emptyMap(),
//    private var matchedPics: Map<String, String> = emptyMap(),
//    private var placeholderMessage: String = ""
//) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val bookNameTextView: TextView = itemView.findViewById(R.id.bookNameTextView)
//        val borrowedDateTextView: TextView = itemView.findViewById(R.id.borrowedDateTextView)
//        val returnDateTextView: TextView = itemView.findViewById(R.id.returnDateTextView)
//        val bookImageView: ImageView = itemView.findViewById(R.id.bookImageView)
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_history, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        if (bookTransactions.isEmpty()) {
//            // Show placeholder message when there is no data
//            holder.bookNameTextView.text = placeholderMessage
//            holder.borrowedDateTextView.text = ""
//            holder.returnDateTextView.text = ""
//            holder.bookImageView.setImageResource(0) // Optionally hide the image
//        } else {
//            // Bind data to TextViews
//            val transaction = bookTransactions[position]
//            val copyId = transaction.copyId
//            val matchedTitle = matchedTitles[copyId] ?: "Title not found"
//            val matchedPic = matchedPics[copyId] ?: "DefaultPic"
//
//            holder.bookNameTextView.text = "$matchedTitle"
//            holder.borrowedDateTextView.text = "Borrowed Date: ${transaction.borrowedDate}"
//            holder.returnDateTextView.text = "Return Date: ${transaction.returnDate}"
//
//            // Set image based on the matched pic value from the books node
//            val drawableResourceId = holder.itemView.context.resources.getIdentifier(
//                matchedPic,
//                "drawable",
//                holder.itemView.context.packageName
//            )
//            holder.bookImageView.setImageResource(drawableResourceId)
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return bookTransactions.size
//    }
//
//    fun updateData(newData: List<BookTransaction>, matchedTitles: Map<String, String>, matchedPics: Map<String, String>, placeholderMessage: String) {
//        bookTransactions = newData
//        this.matchedTitles = matchedTitles
//        this.matchedPics = matchedPics
//        this.placeholderMessage = placeholderMessage
//        notifyDataSetChanged()
//    }
//
//
//    // Function to fetch book title based on copyId
//    private fun fetchBookTitle(copyId: String): String {
//        // Implement logic to fetch title from the 'books' node using copyId
//        // You may need to query your Firebase database here
//        return "Title Not Found"  // Replace this with your actual implementation
//    }
//}
//
//
//
