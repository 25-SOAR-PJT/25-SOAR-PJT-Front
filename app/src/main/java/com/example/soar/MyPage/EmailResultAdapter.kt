package com.example.soar.MyPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.EmailResultItem
import com.example.soar.databinding.ItemIdResultBinding

class EmailResultAdapter
    : ListAdapter<EmailResultItem, EmailResultAdapter.VH>(DIFF) {

    inner class VH(
        private val binding: ItemIdResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EmailResultItem) {
            binding.email.text = item.email   // item_id_result.xml 내 TextView id가 email인 경우
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemIdResultBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EmailResultItem>() {
            override fun areItemsTheSame(o: EmailResultItem, n: EmailResultItem) = o.email == n.email
            override fun areContentsTheSame(o: EmailResultItem, n: EmailResultItem) = o == n
        }
    }
}

