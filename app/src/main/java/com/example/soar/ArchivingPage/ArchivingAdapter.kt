package com.example.soar.ArchivingPage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.Business
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.R
import com.example.soar.databinding.CustomToastBinding
import com.example.soar.databinding.ItemCalendarScheduleBinding

class ArchivingAdapter(
    private val onPolicyClick: (BookmarkedPolicy) -> Unit,
    private val onApplyClick: (BookmarkedPolicy) -> Unit
) :
    ListAdapter<BookmarkedPolicy, ArchivingAdapter.ScheduleViewHolder>(DiffCallback) {


    inner class ScheduleViewHolder(val binding: ItemCalendarScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)


    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<BookmarkedPolicy>() {
            override fun areItemsTheSame(
                oldItem: BookmarkedPolicy,
                newItem: BookmarkedPolicy
            ): Boolean {
                return oldItem.policyId == newItem.policyId
            }

            override fun areContentsTheSame(
                oldItem: BookmarkedPolicy,
                newItem: BookmarkedPolicy
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemCalendarScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val context = holder.itemView.context
        val policy = getItem(position)


        holder.binding.textTitle.text = policy.policyName
        holder.binding.textLabel.text = policy.dateLabel


        val colorRes = when (policy.dateType) {

            "ONGOING", "UPCOMING" -> R.color.semantic_accent_primary_based // Missing expression added here
            "DEADLINE" -> if (policy.applied) {
                R.color.semantic_accent_primary_based
            } else {
                R.color.semantic_accent_deadline_based
            }

            else -> R.color.ref_gray_400
        }

        val color = ContextCompat.getColor(context, colorRes)
        holder.binding.textLabel.setTextColor(color)
        holder.binding.colorTag.setBackgroundColor(color)

        val checkboxDrawable = if (policy.applied)
            R.drawable.icon_checkbox_checked
        else
            R.drawable.icon_checkbox

        holder.binding.checkbox.setImageResource(checkboxDrawable)

        // 체크박스 클릭 이벤트
        holder.binding.checkbox.setOnClickListener {
            onApplyClick(policy)
        }

        holder.binding.contentArea.setOnClickListener {
            // 생성자로 전달받은 람다 함수를 호출하여 클릭된 policy 객체를 전달합니다.
            onPolicyClick(policy)
        }

    }
}
