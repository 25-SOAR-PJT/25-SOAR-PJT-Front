package com.example.soar.CalendarPage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.CustomToastBinding
import com.example.soar.databinding.ItemCalendarScheduleBinding
import java.time.LocalDate

class CalendarScheduleAdapter(
    private val scheduleList: List<Schedule>
) : RecyclerView.Adapter<CalendarScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(val binding: ItemCalendarScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemCalendarScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]
        val context = holder.itemView.context

        holder.binding.textTitle.text = schedule.title
        holder.binding.textLabel.text = when (schedule.type) {
            0 -> context.getString(R.string.always)
            1 -> context.getString(R.string.biz_deadline)
            2 -> context.getString(R.string.apply_deadline)
            else -> context.getString(R.string.biz_end)
        }

        val colorRes = when (schedule.type) {
            0, 1 -> R.color.semantic_accent_primary_based
            2 -> R.color.semantic_accent_deadline_based
            else -> R.color.ref_gray_400
        }

        val color = ContextCompat.getColor(context, colorRes)
        holder.binding.textLabel.setTextColor(color)
        holder.binding.colorTag.setBackgroundColor(color)

        val checkboxDrawable = if (schedule.isApplied)
            R.drawable.icon_checkbox_checked
        else
            R.drawable.icon_checkbox

        holder.binding.checkbox.setImageResource(checkboxDrawable)

        // 체크박스 클릭 시 상태 토글 및 이미지 변경
        holder.binding.checkbox.setOnClickListener {
            schedule.isApplied = !schedule.isApplied
            notifyItemChanged(position)

            if (schedule.isApplied) {
                var toast: Toast? = null
                toast = Toast(holder.itemView.context).apply {
                    duration = Toast.LENGTH_SHORT
                    view = CustomToastBinding.inflate(LayoutInflater.from(holder.itemView.context)).apply {
                        textMessage.text = context.getString(R.string.toast_biz_apply)
                        btnCancel.setOnClickListener {
                            toast?.cancel()
                        }
                    }.root
                }
                toast.show()
            }

        }
    }

    override fun getItemCount(): Int = scheduleList.size
}
