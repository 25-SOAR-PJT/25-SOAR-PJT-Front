package com.example.soar.CalendarPage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.databinding.FragmentCalendarBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarDateAdapter: CalendarDateAdapter
    private lateinit var scheduleAdapter: CalendarScheduleAdapter

    private var currentYearMonth: YearMonth = YearMonth.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupListeners()
        setupObservers()

        updateCalendarDisplay(currentYearMonth)
        viewModel.fetchBookmarkedPolicies()
    }

    private fun setupRecyclerViews() {
        scheduleAdapter = CalendarScheduleAdapter(
            onPolicyClick = { policy ->
                onPolicyItemClick(policy)
            },
            onApplyClick = { policy ->
                // ViewModel의 API 호출 함수를 실행
                viewModel.togglePolicyApplied(policy.policyId)
            }
        )
        binding.bizList.adapter = scheduleAdapter
        binding.bizList.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화 시 ViewModel에서 받은 올바른 타입의 데이터를 전달
        calendarDateAdapter = CalendarDateAdapter(
            dateList = emptyList(),
            recordTypeMap = emptyMap(),
            selectedDate = LocalDate.now()
        ) { clickedDate ->
            viewModel.selectDate(clickedDate)
        }
        binding.date.adapter = calendarDateAdapter
        binding.date.layoutManager = GridLayoutManager(requireContext(), 7)
    }

    private fun setupListeners() {
        binding.btnToday.setOnClickListener {
            currentYearMonth = YearMonth.now()
            viewModel.selectDate(LocalDate.now())
            updateCalendarDisplay(currentYearMonth)
        }
        binding.leftArrow.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            updateCalendarDisplay(currentYearMonth)
        }
        binding.rightArrow.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            updateCalendarDisplay(currentYearMonth)
        }
    }

    private fun setupObservers() {
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            updateSelectedDateHeader(date)
            calendarDateAdapter.updateSelectedDate(date)
        }

        viewModel.eventsByDate.observe(viewLifecycleOwner) { eventsMap ->
            // 올바른 타입의 Map을 어댑터에 전달
            calendarDateAdapter.updateEvents(eventsMap)
        }

        viewModel.schedulesForSelectedDate.observe(viewLifecycleOwner) { schedules ->
            scheduleAdapter.submitList(schedules)
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 달력 UI를 업데이트하는 로직을 별도 함수로 분리
    private fun updateCalendarDisplay(yearMonth: YearMonth) {
        binding.calendarYearText.text = yearMonth.year.toString()
        binding.calendarMonthText.text = yearMonth.monthValue.toString().padStart(2, '0')
        val dateList = DateUtils.generateDateList(yearMonth.year, yearMonth.monthValue)
        calendarDateAdapter.updateDateList(dateList)
    }

    private fun updateSelectedDateHeader(date: LocalDate) {
        val day = date.dayOfMonth
        val dayOfWeekKorean = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
        binding.textToday.text = "${day}일 $dayOfWeekKorean"
    }

    // 2. DetailPageActivity로 이동하는 함수를 추가합니다.
    private fun onPolicyItemClick(policy: BookmarkedPolicy) {
        val intent = Intent(requireContext(), DetailPageActivity::class.java).apply {
            putExtra("policyId", policy.policyId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}