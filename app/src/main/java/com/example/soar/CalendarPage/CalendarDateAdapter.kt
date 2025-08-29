package com.example.soar.CalendarPage

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.R
import com.example.soar.databinding.TileCalendarBinding
import java.time.LocalDate

class CalendarDateAdapter(
    // 1. 생성자에서 받는 데이터 타입을 List<BookmarkedPolicy>로 변경
    private var dateList: List<LocalDate?>,
    private var recordTypeMap: Map<LocalDate, List<BookmarkedPolicy>>,
    private var selectedDate: LocalDate?,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder>() {

    inner class DateViewHolder(val binding: TileCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {
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

            val policiesOnDate = recordTypeMap[date] ?: emptyList()


            if (policiesOnDate.isNotEmpty()) {
                holder.binding.extraCircle.visibility = View.VISIBLE
                val colorResIds = mutableListOf<Int>()

                // CASE 1: 정책이 3개 이하인 경우
                if (policiesOnDate.size <= 3) {
                    policiesOnDate.forEach { policy ->
                        val color = if (policy.applied) {
                            R.color.semantic_accent_primary_based
                        } else {
                            when (policy.dateType) {
                                "ONGOING", "UPCOMING" -> R.color.semantic_accent_primary_based
                                "DEADLINE" -> R.color.semantic_accent_deadline_based
                                else -> R.color.ref_gray_400
                            }
                        }
                        colorResIds.add(color)
                    }
                }
                // CASE 2: 정책이 3개를 초과하는 경우
                else {
                    val deadlinePolicies = policiesOnDate.filter { it.dateType == "DEADLINE" }

                    // 예외: DEADLINE 정책이 없는 경우, 처음 3개에 대해 단순 표시
                    if (deadlinePolicies.isEmpty()) {
                        policiesOnDate.take(3).forEach { policy ->
                            val color = if (policy.applied) {
                                R.color.semantic_accent_primary_based
                            } else {
                                when (policy.dateType) {
                                    "ONGOING", "UPCOMING" -> R.color.semantic_accent_primary_based
                                    "DEADLINE" -> R.color.semantic_accent_deadline_based
                                    else -> R.color.ref_gray_400
                                }
                            }
                            colorResIds.add(color)
                        }
                    }
                    // DEADLINE 정책이 있는 경우, 요약 로직 적용
                    else {
                        // 규칙: 첫 번째 서클 (완료 건 존재 여부)
                        if (deadlinePolicies.any { it.applied }) {
                            colorResIds.add(R.color.semantic_accent_primary_based)
                        }
                        else{
                            colorResIds.add(R.color.semantic_accent_deadline_based)
                        }

                        // 규칙: 두 번째 서클 (미완료 비율)
                        val unappliedCount = deadlinePolicies.count { !it.applied }.toDouble()
                        val totalCount = deadlinePolicies.size.toDouble()
                        if (totalCount > 0 && (unappliedCount / totalCount) >= 0.5) {
                            colorResIds.add(R.color.semantic_accent_deadline_based) // 50% 이상이면 빨간색
                        } else {
                            colorResIds.add(R.color.semantic_accent_primary_based) // 50% 미만이면 파란색
                        }

                        // 규칙: 세 번째 서클 (미완료 건 존재 여부)
                        if (deadlinePolicies.any { !it.applied }) {
                            colorResIds.add(R.color.semantic_accent_deadline_based)
                        }
                        else{
                            colorResIds.add(R.color.semantic_accent_primary_based)
                        }
                    }
                }

                // 최종적으로 결정된 색상 리스트로 서클 뷰를 생성
                colorResIds.forEachIndexed { index, colorResId ->
                    val circle = View(context).apply {
                        val size = 5.dpToPx(context)
                        val lp = LinearLayout.LayoutParams(size, size)

                        // 현재 서클이 마지막이 아닐 경우에만 오른쪽 여백을 추가
                        if (index < colorResIds.lastIndex) {
                            lp.rightMargin = 2.dpToPx(context)
                        }

                        layoutParams = lp
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

    // 3. Fragment에서 데이터를 업데이트할 수 있도록 함수 추가
    fun updateDateList(newDateList: List<LocalDate?>) {
        this.dateList = newDateList
        notifyDataSetChanged()
    }

    fun updateEvents(newEvents: Map<LocalDate, List<BookmarkedPolicy>>) {
        this.recordTypeMap = newEvents
        notifyDataSetChanged()
    }

    fun updateSelectedDate(newSelectedDate: LocalDate) {
        this.selectedDate = newSelectedDate
        notifyDataSetChanged()
    }

    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
