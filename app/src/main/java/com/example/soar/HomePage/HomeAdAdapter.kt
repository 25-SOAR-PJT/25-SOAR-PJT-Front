package com.example.soar.HomePage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.databinding.ItemHomeAdBinding

class HomeAdAdapter(private val items: List<adItem>) : RecyclerView.Adapter<HomeAdAdapter.AdViewHolder>() {

    inner class AdViewHolder(val binding: ItemHomeAdBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = ItemHomeAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textLabel.text = item.label
            textTitle.text = item.tile.replace("\\n", "\n")
            // imageView.setImageResource(...) ← 필요 시 이미지 동적 설정 가능
        }
    }

    override fun getItemCount(): Int = items.size
}
