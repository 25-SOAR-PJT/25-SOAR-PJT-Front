package com.example.soar.HomePage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.databinding.ItemHomeAdBinding

// [수정] 생성자에 클릭 리스너 람다 함수 추가
class HomeAdAdapter(
    private val items: List<adItem>,
    private val onItemClick: (adItem) -> Unit
) : RecyclerView.Adapter<HomeAdAdapter.AdViewHolder>() {

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
        }
        // [추가] 아이템 뷰에 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}