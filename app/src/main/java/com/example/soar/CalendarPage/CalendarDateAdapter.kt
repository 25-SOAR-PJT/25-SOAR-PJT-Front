package com.example.soar.CalendarPage

import android.content.Context
import android.content.res.ColorStateList
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
    private val recordTypeMap: Map<LocalDate, List<Int>>, // ← 이게 핵심
    private val selectedDate: LocalDate?,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder>() {

    inner class DateViewHolder(val binding: TileCalendarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: LocalDate?) {
            if (date != null) {
                binding.dateText.text = date.dayOfMonth.toString()
                binding.dateTile.setOnClickListener {
                    onDateClick(date) // 클릭 시 CalendarActivity로 전달
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
            holder.binding.dateText.text = date.dayOfMonth.toString()
            holder.binding.dateText.visibility = View.VISIBLE

            val context = holder.binding.root.context

            // ✅ 날짜 강조
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
            }

            // ✅ extraCircle 초기화
            holder.binding.extraCircle.removeAllViews()

            // ✅ 날짜에 해당하는 데이터 종류 리스트
            val types: List<Int> = recordTypeMap[date] ?: emptyList()

            if (types.isNotEmpty()) {
                holder.binding.extraCircle.visibility = View.VISIBLE

                for (type in types.take(3)) { // 최대 2개까지만 표시
                    val circle = View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(5.dpToPx(context), 5.dpToPx(context)).apply {
                            setMargins(0, 0, 2.dpToPx(context), 0)
                        }
                        background = ContextCompat.getDrawable(context, R.drawable.circle_background)
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                if (type == 0) R.color.semantic_accent_primary_based
                                else R.color.semantic_accent_deadline_based
                            )
                        )
                    }
                    holder.binding.extraCircle.addView(circle)
                }
            } else {
                holder.binding.extraCircle.visibility = View.VISIBLE
            }

        } else {
            holder.binding.dateText.visibility = View.INVISIBLE
            holder.binding.tileBackground.visibility = View.INVISIBLE
            holder.binding.extraCircle.visibility = View.VISIBLE
        }
    }

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    override fun getItemCount(): Int {
        return dateList.size
    }

}