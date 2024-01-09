package com.dr.qck.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dr.qck.database.ExceptionMessage
import com.dr.qck.databinding.ExceptionItemLayoutBinding

class ExceptionListAdapter(
    private val list: MutableList<ExceptionMessage>,
    private val onDeleted: (Int, ExceptionMessage) -> Unit
) : RecyclerView.Adapter<ExceptionListAdapter.ExceptionViewHolder>() {
    class ExceptionViewHolder(binding: ExceptionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val b = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExceptionViewHolder =
        ExceptionViewHolder(
            ExceptionItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ExceptionViewHolder, position: Int) {
        holder.b.senderText.text = list[position].senderName
        holder.b.deleteImage.setOnClickListener {
            onDeleted.invoke(holder.adapterPosition, list[holder.adapterPosition])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(position: Int): Boolean {
        list.removeAt(position)
        notifyItemRemoved(position)
        return list.isEmpty()
    }
}