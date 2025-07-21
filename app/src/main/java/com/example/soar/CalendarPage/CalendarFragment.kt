package com.example.soar.CalendarPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soar.R
import com.example.soar.databinding.FragmentCalendarBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class Schedule(
    val date: LocalDate,
    val title: String,
    val type: Int,
    var isApplied: Boolean = false
)


class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // 화면상으로 보고 있는 연/월
    private var currentYear  = 0
    private var currentMonth = 0

    // 지금 선택되어 있는 날짜 (디폴트는 today)
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onResume() {
        super.onResume()
        setCalendar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedDate = LocalDate.now()
        currentYear = selectedDate.year
        currentMonth = selectedDate.monthValue

        binding.btnToday.setOnClickListener {
            val today = LocalDate.now()
            selectedDate = today
            currentYear = today.year
            currentMonth = today.monthValue

            setCalendar()
        }

        binding.leftArrow.setOnClickListener { prevMonth() }
        binding.rightArrow.setOnClickListener { nextMonth() }

        setCalendar()
    }

    private fun setCalendar() {
        drawCalendarWithMap(recordTypeMap)
        setScheduleList(selectedDate)
    }

    private fun drawCalendarWithMap(recordTypeMap: Map<LocalDate, List<Int>>) {
        val dateList = DateUtils.generateDateList(currentYear, currentMonth)

        binding.date.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.date.adapter = CalendarDateAdapter(
            dateList = dateList,
            recordTypeMap = recordTypeMap,
            selectedDate = selectedDate
        ) { clicked ->
            selectedDate = clicked
            setCalendar()
        }

        binding.calendarYearText.text = currentYear.toString()
        binding.calendarMonthText.text = currentMonth.toString().padStart(2, '0')
    }

    private fun prevMonth() {
        val (y, m) = DateUtils.moveToPreviousMonth(currentYear, currentMonth)
        currentYear  = y
        currentMonth = m

        // 화면 갱신 (달력 + 월 총합)
        setCalendar()

        // 날짜를 바꿔도(월 이동) 히스토리 영역은 초기화
        selectedDate = LocalDate.of(currentYear, currentMonth, 1)
    }

    private fun nextMonth() {
        val (y, m) = DateUtils.moveToNextMonth(currentYear, currentMonth)
        currentYear  = y
        currentMonth = m

        setCalendar()

        selectedDate = LocalDate.of(currentYear, currentMonth, 1)
    }


    private val dummyScheduleList = listOf(
        Schedule(LocalDate.of(2025, 7, 1), "청년마음건강지원사업 이용자 모집", 1),
        Schedule(LocalDate.of(2025, 7, 1), "청년마음건강지원사업 이용자 모집", 0),
        Schedule(LocalDate.of(2025, 7, 15), "청년마음건강지원사업 이용자 모집2", 1),
        Schedule(LocalDate.of(2025, 7, 15), "청년마음건강지원사업 이용자 모집", 1),
        Schedule(LocalDate.of(2025, 7, 15), "청년마음건강지원사업 이용자 모집", 2),
        Schedule(LocalDate.of(2025, 7, 30), "청년마음건강지원사업 이용자 모집", 2)
        // 0 = 상시, 1 = 사업 마감일, 2 = 신청 마감일, 3 = 사업 종료
    )

    private val recordTypeMap: Map<LocalDate, List<Int>> = dummyScheduleList
        .groupBy { it.date }
        .mapValues { entry ->
            entry.value.map { it.type }
        }


    private fun setScheduleList(date: LocalDate) {
        val day = date.dayOfMonth
        val dayOfWeekKorean = when (date.dayOfWeek) {
            DayOfWeek.MONDAY    -> "월요일"
            DayOfWeek.TUESDAY   -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY  -> "목요일"
            DayOfWeek.FRIDAY    -> "금요일"
            DayOfWeek.SATURDAY  -> "토요일"
            DayOfWeek.SUNDAY    -> "일요일"
        }

        binding.textToday.text = "${day}일 $dayOfWeekKorean"

        val filteredList = dummyScheduleList.filter { it.date == selectedDate }
        binding.bizList.adapter = CalendarScheduleAdapter(filteredList)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}