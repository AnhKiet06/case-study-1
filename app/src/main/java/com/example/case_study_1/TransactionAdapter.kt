package com.example.case_study_1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val items: MutableList<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val onItemLongClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivItemIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvItemCategory)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvAmount: TextView = view.findViewById(R.id.tvItemAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]

        holder.ivIcon.setImageResource(TransactionRepository.getCategoryIcon(item.category))
        holder.tvTitle.text = item.note.ifEmpty { item.category }
        holder.tvCategory.text = item.category
        holder.tvDate.text = item.date

        if (item.isExpense) {
            holder.tvAmount.text = "-${TransactionRepository.formatMoney(item.amount)}"
            holder.tvAmount.setTextColor(Color.parseColor("#EF4444"))
        } else {
            holder.tvAmount.text = "+${TransactionRepository.formatMoney(item.amount)}"
            holder.tvAmount.setTextColor(Color.parseColor("#16A34A"))
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Transaction>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}