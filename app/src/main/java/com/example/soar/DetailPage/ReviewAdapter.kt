package com.example.soar.DetailPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.databinding.ItemReviewBinding

class ReviewAdapter(
    private val items: List<Item.Review>,
    private val onOptionsClick: ((position: Int) -> Unit)? = null,
    private val showOptions: Boolean = true
) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item.Review) {
            binding.textUsername.text = item.username
            binding.textContent.text = item.content

            binding.btnCommentEtc.visibility = if (showOptions) View.VISIBLE else View.GONE

            binding.btnCommentEtc.setOnClickListener {
                onOptionsClick?.invoke(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
