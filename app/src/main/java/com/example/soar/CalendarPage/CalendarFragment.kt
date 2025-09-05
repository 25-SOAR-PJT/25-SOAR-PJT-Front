package com.example.soar.CalendarPage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.MainActivity
import com.example.soar.Network.TokenManager
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.databinding.FragmentCalendarBinding
import com.example.soar.util.showBlockingToast // ✨ 1. 커스텀 토스트 import
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

    override fun onResume() {
        super.onResume()
        updateUiForLoginState()
    }

    private fun updateUiForLoginState() {
        val accessToken = TokenManager.getAccessToken()

        if (accessToken.isNullOrEmpty()) {
            binding.scheduleContainer.visibility = View.GONE
            binding.btnZeroEntry.visibility = View.GONE
            binding.btnLoginEntry.visibility = View.VISIBLE

        } else {
            binding.btnLoginEntry.visibility = View.GONE
            viewModel.fetchBookmarkedPolicies()
        }
    }

    private fun setupRecyclerViews() {
        scheduleAdapter = CalendarScheduleAdapter(
            onPolicyClick = { policy ->
                onPolicyItemClick(policy)
            },
            onApplyClick = { policy ->
                viewModel.togglePolicyApplied(policy.policyId)
            }
        )
        binding.bizList.adapter = scheduleAdapter
        binding.bizList.layoutManager = LinearLayoutManager(requireContext())

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
        binding.btnToLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnToExplore.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.goToExploreTab()
        }
    }

    private fun setupObservers() {
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            updateSelectedDateHeader(date)
            calendarDateAdapter.updateSelectedDate(date)
        }

        viewModel.eventsByDate.observe(viewLifecycleOwner) { eventsMap ->
            calendarDateAdapter.updateEvents(eventsMap)
        }

        viewModel.schedulesForSelectedDate.observe(viewLifecycleOwner) { schedules ->
            scheduleAdapter.submitList(schedules)
        }

        // ✨ 2. toastEvent 옵저버를 커스텀 토스트를 사용하도록 수정
        viewModel.toastEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { toastInfo ->
                // '되돌리기' 액션을 포함하여 커스텀 토스트를 띄웁니다.
                showBlockingToast(
                    message = toastInfo.message,
                    cancelText = toastInfo.cancelText,
                    onCancel = toastInfo.onCancel
                )
            }
        }

        viewModel.allPolicies.observe(viewLifecycleOwner) { allPolicies ->
            if (allPolicies.isEmpty()) {
                binding.scheduleContainer.visibility = View.GONE
                binding.btnZeroEntry.visibility = View.VISIBLE
            } else {
                binding.scheduleContainer.visibility = View.VISIBLE
                binding.btnZeroEntry.visibility = View.GONE
            }
        }
    }

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