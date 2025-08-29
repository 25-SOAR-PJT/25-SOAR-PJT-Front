package com.example.soar.MyPage

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.Network.archiving.AppliedPolicy
import com.example.soar.R
import com.example.soar.databinding.ItemAppliedBizBinding

class AppliedAdapter :
    ListAdapter<AppliedPolicy, AppliedAdapter.ScheduleViewHolder>(DiffCallback) {

    inner class ScheduleViewHolder(val binding: ItemAppliedBizBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AppliedPolicy>() {
            override fun areItemsTheSame(oldItem: AppliedPolicy, newItem: AppliedPolicy): Boolean {
                return oldItem.policyId == newItem.policyId
            }
            override fun areContentsTheSame(oldItem: AppliedPolicy, newItem: AppliedPolicy): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemAppliedBizBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val context = holder.itemView.context
        val policy = getItem(position)

        holder.binding.textTitle.text = policy.policyName

        // dateLabel 파싱 로직
        val dDayMarker = "D-"
        if (policy.dateLabel.contains(dDayMarker)) {
            val parts = policy.dateLabel.split(dDayMarker, limit = 2)
            holder.binding.appliedDateType.visibility = View.VISIBLE
            holder.binding.appliedDateType.text = parts[0]
            holder.binding.appliedDday.text = dDayMarker + parts[1]
            holder.binding.appliedDateType.setTextColor(ContextCompat.getColor(context, R.color.ref_blue_600))
        } else {
            holder.binding.appliedDateType.visibility = View.GONE
            holder.binding.appliedDday.text = policy.dateLabel
        }

        // dateType에 따른 색상 적용
        val (bgColor, textColor) = when (policy.dateType) {
            "ONGOING"-> Pair(
                Color.parseColor("#E9F0E6"),
                Color.parseColor("#245E13")
            )
            "UPCOMING", "DEADLINE" -> Pair(
                ContextCompat.getColor(context, R.color.ref_blue_100),
                ContextCompat.getColor(context, R.color.ref_blue_500)
            )
            else -> Pair(
                ContextCompat.getColor(context, R.color.ref_coolgray_100),
                ContextCompat.getColor(context, R.color.ref_coolgray_500)
            )
        }
        holder.binding.appliedDday.backgroundTintList = ColorStateList.valueOf(bgColor)
        holder.binding.appliedDday.setTextColor(textColor)

        // 아이템 클릭 시 상세 페이지로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPageActivity::class.java).apply {
                putExtra("policyId", policy.policyId)
            }
            context.startActivity(intent)
        }
    }
}