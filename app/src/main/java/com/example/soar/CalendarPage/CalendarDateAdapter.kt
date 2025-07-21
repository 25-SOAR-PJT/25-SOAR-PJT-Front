package com.example.soar.CalendarPage

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.R
import com.example.soar.databinding.TileCalendarBinding
import java.time.LocalDate

class CalendarDateAdapter(
    private val dateList: List<LocalDate?>,
    private val recordTypeMap: Map<LocalDate, List<Int>>,
    private val selectedDate: LocalDate?,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder>() {

    inner class DateViewHolder(val binding: TileCalendarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: LocalDate?) {
            if (date != null) {
                binding.dateText.text = date.dayOfMonth.toString()
                binding.dateTile.setOnClickListener {
                    onDateClick(date)
                }
            } else {
                binding.dateText.text = ""
                binding.dateText.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TileCalendarBinding.inflate(inflater, parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dateList[position]
        holder.bind(date)

        if (date != null) {
            val context = holder.binding.root.context
            holder.binding.dateText.visibility = View.VISIBLE
            holder.binding.dateText.text = date.dayOfMonth.toString()

            // 날짜 선택 표시
            if (date == selectedDate) {
                holder.binding.tileBackground.visibility = View.VISIBLE
                holder.binding.tileBackground.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.ref_gray_100)
                )
                holder.binding.dateText.setTextColor(
                    ContextCompat.getColor(context, R.color.semantic_text_strong)
                )
            } else {
                holder.binding.tileBackground.visibility = View.INVISIBLE
                holder.binding.dateText.setTextColor(
                    ContextCompat.getColor(context, R.color.semantic_text_primary)
                )
            }

            // 배지 표시
            holder.binding.extraCircle.removeAllViews()
            val types = recordTypeMap[date] ?: emptyList()

            if (types.isNotEmpty()) {
                holder.binding.extraCircle.visibility = View.VISIBLE
                for (type in types.take(3)) {
                    val colorResId = when (type) {
                        0, 1 -> R.color.semantic_accent_primary_based
                        2 -> R.color.semantic_accent_deadline_based
                        else -> R.color.ref_gray_400
                    }
                    val circle = View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(5.dpToPx(context), 5.dpToPx(context)).apply {
                            setMargins(0, 0, 2.dpToPx(context), 0)
                        }
                        background = ContextCompat.getDrawable(context, R.drawable.circle_background)
                        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorResId))
                    }
                    holder.binding.extraCircle.addView(circle)
                }
            } else {
                holder.binding.extraCircle.visibility = View.GONE
            }

        } else {
            holder.binding.dateText.visibility = View.INVISIBLE
            holder.binding.tileBackground.visibility = View.INVISIBLE
            holder.binding.extraCircle.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = dateList.size

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
