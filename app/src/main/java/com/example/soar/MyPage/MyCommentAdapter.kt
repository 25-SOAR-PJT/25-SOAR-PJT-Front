// MyPage/MyCommentAdapter.kt
package com.example.soar.MyPage

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.DetailPage.ReviewDetailActivity
import com.example.soar.Network.detail.CommentResponse
import com.example.soar.databinding.ItemMyCommentBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyCommentAdapter : ListAdapter<CommentResponse, MyCommentAdapter.MyCommentViewHolder>(DiffCallback) {

    inner class MyCommentViewHolder(private val binding: ItemMyCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentResponse) {
            binding.policyName.text = comment.policyName ?: "정책 이름 없음"
            binding.myCommentContent.text = comment.comment

            // policy_comment_num은 API 응답에 없으므로, 작성일로 대체하여 표시합니다.
            try {
                val parsedDate = LocalDateTime.parse(comment.createdDate)
                binding.policyCommentNum.text = parsedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            } catch (e: Exception) {
                binding.policyCommentNum.text = "" // 파싱 실패 시 비워둠
            }

            // 아이템 클릭 시 해당 정책의 전체 댓글 보기 화면으로 이동
            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, ReviewDetailActivity::class.java).apply {
                    putExtra("policyId", comment.policyId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCommentViewHolder {
        val binding = ItemMyCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyCommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CommentResponse>() {
            override fun areItemsTheSame(old: CommentResponse, new: CommentResponse) = old.commentId == new.commentId
            override fun areContentsTheSame(old: CommentResponse, new: CommentResponse) = old == new
        }
    }
}