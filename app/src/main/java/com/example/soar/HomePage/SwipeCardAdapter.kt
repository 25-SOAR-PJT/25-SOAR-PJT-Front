package com.example.soar.HomePage

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.ItemSwipeCardBinding

class SwipeCardAdapter(private val items: List<SwipeCardItem>) :
    RecyclerView.Adapter<SwipeCardAdapter.SwipeCardViewHolder>() {

    inner class SwipeCardViewHolder(val binding: ItemSwipeCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSwipeCardBinding.inflate(inflater, parent, false)
        return SwipeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SwipeCardViewHolder, position: Int) {
        val item = items[position]

        holder.binding.itemContainer.setBackgroundResource(item.imageResId)

        holder.binding.textTitle.text = item.title
        holder.binding.btnLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}
