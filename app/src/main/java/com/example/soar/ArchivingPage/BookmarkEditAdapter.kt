// ArchivingPage/BookmarkEditAdapter.kt

package com.example.soar.ArchivingPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.ItemEditArchivingBinding

class BookmarkEditAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<EditPolicyUiModel, BookmarkEditAdapter.PolicyViewHolder>(DiffCallback) {

    inner class PolicyViewHolder(val binding: ItemEditArchivingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.checkbox.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition).policy.policyId)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<EditPolicyUiModel>() {
            override fun areItemsTheSame(oldItem: EditPolicyUiModel, newItem: EditPolicyUiModel): Boolean {
                return oldItem.policy.policyId == newItem.policy.policyId
            }

            override fun areContentsTheSame(oldItem: EditPolicyUiModel, newItem: EditPolicyUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolicyViewHolder {
        val binding = ItemEditArchivingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PolicyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PolicyViewHolder, position: Int) {
        val uiModel = getItem(position)
        val policy = uiModel.policy
        val context = holder.itemView.context

        holder.binding.textTitle.text = policy.policyName
        holder.binding.textLabel.text = policy.dateLabel

        // ArchivingAdapter와 동일한 색상 로직 적용
        val colorRes = when (policy.dateType) {
            "ONGOING", "UPCOMING" -> R.color.semantic_accent_primary_based
            "DEADLINE" -> if (policy.applied) {
                // ✨ 5. 'policy.applied'가 이제 true이므로,
                // 이전에 선택했던 deadline 색상 대신 primary 색상이 선택됩니다. (색상 반전)
                R.color.semantic_accent_primary_based
            } else {
                R.color.semantic_accent_deadline_based
            }
            else -> R.color.ref_gray_400
        }
        val color = ContextCompat.getColor(context, colorRes)
        holder.binding.textLabel.setTextColor(color)
        holder.binding.colorTag.setBackgroundColor(color)

        // isSelected 상태에 따라 체크박스 UI 변경
        val checkboxDrawable = if (uiModel.isSelected)
            R.drawable.ic_checkbox_active
        else
            R.drawable.ic_checkbox_inactive
        holder.binding.checkbox.setImageResource(checkboxDrawable)
    }


}