// HomePage/PersonalCardAdapter.kt

package com.example.soar.HomePage

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.Network.home.AgePopularPolicy
import com.example.soar.R
import com.example.soar.databinding.ItemHomeAd2Binding

class PersonalCardAdapter(
    private val onBookmarkClick: (AgePopularPolicy) -> Unit
) : ListAdapter<AgePopularPolicy, PersonalCardAdapter.CardViewHolder>(DiffCallback) {

    inner class CardViewHolder(private val binding: ItemHomeAd2Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(policy: AgePopularPolicy) {
            binding.location.text = policy.supervisingInstName
            binding.title.text = policy.policyName
            binding.dday.text = policy.dateLabel

            val bookmarkRes = if (policy.bookmarked) {
                R.drawable.icon_bookmark_checked
            } else {
                R.drawable.icon_bookmark
            }
            binding.btnBookmark.setImageResource(bookmarkRes)

            binding.btnBookmark.setOnClickListener {
                onBookmarkClick(policy)
            }

            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, DetailPageActivity::class.java).apply {
                    putExtra("policyId", policy.policyId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemHomeAd2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AgePopularPolicy>() {
            override fun areItemsTheSame(oldItem: AgePopularPolicy, newItem: AgePopularPolicy): Boolean {
                return oldItem.policyId == newItem.policyId
            }
            override fun areContentsTheSame(oldItem: AgePopularPolicy, newItem: AgePopularPolicy): Boolean {
                return oldItem == newItem
            }
        }
    }
}