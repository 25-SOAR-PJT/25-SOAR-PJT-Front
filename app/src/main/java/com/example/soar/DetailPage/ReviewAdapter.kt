// DetailPage/ReviewAdapter.kt
package com.example.soar.DetailPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.Network.TokenManager
import com.example.soar.Network.detail.CommentResponse
import com.example.soar.databinding.ItemReviewBinding
import java.time.format.DateTimeFormatter

class ReviewAdapter(
    private val onOptionsClick: (CommentResponse) -> Unit
) : ListAdapter<CommentResponse, ReviewAdapter.ReviewViewHolder>(DiffCallback) {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentResponse) {
            binding.textUsername.text = comment.userName
            binding.textContent.text = comment.comment


            // 내가 쓴 댓글에만 ... (더보기) 버튼이 보이도록 처리
            val myUserId = TokenManager.getUserId()// Long 타입 반환
            if (comment.userId == myUserId.toString()) {
                binding.btnCommentEtc.visibility = View.VISIBLE
                binding.btnCommentEtc.setOnClickListener {
                    onOptionsClick(comment)
                }
            } else {
                binding.btnCommentEtc.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CommentResponse>() {
            override fun areItemsTheSame(old: CommentResponse, new: CommentResponse) = old.commentId == new.commentId
            override fun areContentsTheSame(old: CommentResponse, new: CommentResponse) = old == new
        }
    }
}