package com.example.my_gpt.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.my_gpt.R

class ImageAdapter (
    private val historyItems: MutableList<String>,
    private val itemClickListener: (String) -> Unit,
    private val itemDeleteListener: (Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewItem)

        init {
            itemView.setOnClickListener {
                itemClickListener(historyItems[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.textView.text = historyItems[position]
    }

    override fun getItemCount() = historyItems.size

    fun removeItem(position: Int) {
        historyItems.removeAt(position)
        notifyItemRemoved(position)
        itemDeleteListener(position)
    }
}