package com.example.soar.CalendarPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.ArchivingPage.Event
import com.example.soar.Network.archiving.ArchivingRepository
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.Network.archiving.ToggleApplyResponse
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarViewModel : ViewModel() {

    private val repository = ArchivingRepository()

    // 서버에서 가져온 전체 북마크 리스트
    private val _allPolicies = MutableLiveData<List<BookmarkedPolicy>>()
    val allPolicies: LiveData<List<BookmarkedPolicy>> get() = _allPolicies

    // 현재 선택된 날짜
    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> get() = _selectedDate

    // 날짜별로 그룹화된 정책 맵 (캘린더의 점 표시용)
    private val _eventsByDate = MutableLiveData<Map<LocalDate, List<BookmarkedPolicy>>>()
    val eventsByDate: LiveData<Map<LocalDate, List<BookmarkedPolicy>>> get() = _eventsByDate

    // 선택된 날짜에 해당하는 정책 리스트
    private val _schedulesForSelectedDate = MutableLiveData<List<BookmarkedPolicy>>()
    val schedulesForSelectedDate: LiveData<List<BookmarkedPolicy>> get() = _schedulesForSelectedDate

    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> get() = _toastEvent

    fun fetchBookmarkedPolicies() {
        viewModelScope.launch {
            repository.getBookmarkedPolicies()
                .onSuccess { policies ->
                    _allPolicies.value = policies
                    updateEventsMap(policies)
                    updateSchedulesForDate(selectedDate.value ?: LocalDate.now(), policies)
                }
                .onFailure {
                    // 에러 처리
                }
        }
    }

    // 날짜를 클릭했을 때 호출
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateSchedulesForDate(date, _allPolicies.value ?: emptyList())
    }

    // 서버에서 받은 데이터를 날짜별로 그룹화
    private fun updateEventsMap(policies: List<BookmarkedPolicy>) {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        _eventsByDate.value = policies.mapNotNull { policy ->
            try {
                // businessPeriodEnd가 비어있지 않은 경우에만 파싱
                if (policy.businessPeriodEnd.isNotBlank()) {
                    val date = LocalDate.parse(policy.businessPeriodEnd.trim(), formatter)
                    date to policy
                } else {
                    null
                }
            } catch (e: Exception) {
                null // 날짜 형식이 잘못된 경우 무시
            }
        }.groupBy({ it.first }, { it.second })
    }

    // 선택된 날짜에 맞는 스케줄 리스트 업데이트
    private fun updateSchedulesForDate(date: LocalDate, policies: List<BookmarkedPolicy>) {
        _schedulesForSelectedDate.value = eventsByDate.value?.get(date) ?: emptyList()
    }

    // 신청 상태 토글 API 호출
    fun togglePolicyApplied(policyId: String) {
        viewModelScope.launch {
            repository.togglePolicyApply(policyId)
                .onSuccess { response ->
                    _toastEvent.value = Event(response.message)
                    // API 호출 성공 후 전체 데이터를 다시 불러와 갱신
                    fetchBookmarkedPolicies()
                }
                .onFailure {
                    _toastEvent.value = Event(it.message ?: "작업에 실패했습니다.")
                }
        }
    }
}