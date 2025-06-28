// KeywordAdapter.kt
package com.example.soar.DetailPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.databinding.ItemKeywordBinding

class KeywordAdapter(private val items: List<Item.Keyword>) :
    RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder>() {

    inner class KeywordViewHolder(private val binding: ItemKeywordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item.Keyword) {
            binding.textKeyword.text = item.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val binding = ItemKeywordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
