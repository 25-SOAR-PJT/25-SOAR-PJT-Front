package com.example.soar.ExplorePage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.R
import com.example.soar.databinding.ItemExploreBizBinding
import com.example.soar.Network.TokenManager

// isBookmarkEnabled 파라미터 추가
class ExploreAdapter(
    private val listener: OnItemClickListener,
    private val isBookmarkEnabled: Boolean = true
) : ListAdapter<YouthPolicy, ExploreAdapter.PolicyViewHolder>(PolicyDiffCallback()) {

    interface OnItemClickListener {
        fun onPolicyItemClick(policy: YouthPolicy)
        fun onBookmarkClick(policy: YouthPolicy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolicyViewHolder {
        val binding = ItemExploreBizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PolicyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PolicyViewHolder, position: Int) {
        holder.bind(getItem(position), listener, isBookmarkEnabled) // isBookmarkEnabled 전달
    }

    class PolicyViewHolder(private val binding: ItemExploreBizBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(policy: YouthPolicy, listener: OnItemClickListener, isBookmarkEnabled: Boolean) {
            binding.textViewPolicyName.text = policy.policyName
            binding.textViewPolicyRegion.text = policy.supervisingInstName
            binding.largeClassification.text = policy.largeClassification
            binding.mediumClassification.text = policy.mediumClassification

            if (!policy.dateLabel.isNullOrEmpty()) {
                binding.textViewPolicyDeadline.visibility = View.VISIBLE
                binding.textViewPolicyDeadline.text = policy.dateLabel
            } else {
                binding.textViewPolicyDeadline.visibility = View.GONE
            }

            if (policy.bookmarked == true) {
                binding.btnBookmark.setImageResource(R.drawable.icon_bookmark_checked)
            } else {
                binding.btnBookmark.setImageResource(R.drawable.icon_bookmark)
            }
            binding.bookmarkProgressBar.visibility = View.GONE
            binding.btnBookmark.visibility = View.VISIBLE

            itemView.setOnClickListener {
                listener.onPolicyItemClick(policy)
            }

            // isBookmarkEnabled 값에 따라 버튼의 클릭 리스너를 설정
            if (isBookmarkEnabled) {
                binding.btnBookmark.isEnabled = true
                binding.btnBookmark.setOnClickListener {
                    val accessToken = TokenManager.getAccessToken()

                    if (!accessToken.isNullOrEmpty()) {
                        binding.btnBookmark.visibility = View.GONE
                        binding.bookmarkProgressBar.visibility = View.VISIBLE
                        binding.btnBookmark.isEnabled = false

                        listener.onBookmarkClick(policy)
                    } else {
                        Toast.makeText(binding.root.context, "로그인 후 이용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // 북마크 기능이 필요 없는 경우
                binding.btnBookmark.isEnabled = false
                binding.btnBookmark.setOnClickListener(null) // 리스너를 null로 설정하여 클릭 이벤트 제거
            }
        }
    }
}

class PolicyDiffCallback : DiffUtil.ItemCallback<YouthPolicy>() {
    override fun areItemsTheSame(oldItem: YouthPolicy, newItem: YouthPolicy): Boolean {
        return oldItem.policyId == newItem.policyId
    }

    override fun areContentsTheSame(oldItem: YouthPolicy, newItem: YouthPolicy): Boolean {
        return oldItem == newItem
    }
}