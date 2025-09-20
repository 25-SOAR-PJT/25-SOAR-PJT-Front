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

// ✨ 1. 토스트에 메시지와 '되돌리기' 액션을 함께 전달하기 위한 데이터 클래스
data class ToastInfo(
    val message: String,
    val cancelText: String? = null,
    val onCancel: (() -> Unit)? = null
)

class CalendarViewModel : ViewModel() {

    private val repository = ArchivingRepository()

    private val _allPolicies = MutableLiveData<List<BookmarkedPolicy>>()
    val allPolicies: LiveData<List<BookmarkedPolicy>> get() = _allPolicies

    private val _selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedDate: LiveData<LocalDate> get() = _selectedDate

    private val _eventsByDate = MutableLiveData<Map<LocalDate, List<BookmarkedPolicy>>>()
    val eventsByDate: LiveData<Map<LocalDate, List<BookmarkedPolicy>>> get() = _eventsByDate

    private val _schedulesForSelectedDate = MutableLiveData<List<BookmarkedPolicy>>()
    val schedulesForSelectedDate: LiveData<List<BookmarkedPolicy>> get() = _schedulesForSelectedDate

    // ✨ 2. LiveData가 String 대신 ToastInfo 객체를 전달하도록 변경
    private val _toastEvent = MutableLiveData<Event<ToastInfo>>()
    val toastEvent: LiveData<Event<ToastInfo>> get() = _toastEvent

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

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateSchedulesForDate(date, _allPolicies.value ?: emptyList())
    }

    private fun updateEventsMap(policies: List<BookmarkedPolicy>) {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        _eventsByDate.value = policies.mapNotNull { policy ->
            try {
                if (policy.businessPeriodEnd.isNotBlank()) {
                    val date = LocalDate.parse(policy.businessPeriodEnd.trim(), formatter)
                    date to policy
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.groupBy({ it.first }, { it.second })
    }

    private fun updateSchedulesForDate(date: LocalDate, policies: List<BookmarkedPolicy>) {
        _schedulesForSelectedDate.value = eventsByDate.value?.get(date) ?: emptyList()
    }

    // ✨ 3. '되돌리기' 로직을 처리하는 함수
    fun undoApply(policyId: String) {
        viewModelScope.launch {
            repository.togglePolicyApply(policyId)
                .onSuccess { response ->
                    // 되돌리기 성공 시에는 토스트를 띄우지 않고 UI만 조용히 업데이트
                    updateLocalPolicyState(response)
                }
                .onFailure {
                    _toastEvent.value = Event(ToastInfo(it.message ?: "되돌리기에 실패했습니다."))
                }
        }
    }

    // ✨ 4. 신청 상태 토글 로직을 로컬 즉시 업데이트 및 '되돌리기' 기능 추가로 개선
    fun togglePolicyApplied(policyId: String) {
        viewModelScope.launch {
            repository.togglePolicyApply(policyId)
                .onSuccess { response ->
                    // UI를 즉시 업데이트
                    updateLocalPolicyState(response)

                    // '되돌리기' 버튼이 포함된 토스트를 띄움
                    _toastEvent.value = Event(ToastInfo(
                        message = response.message,
                        cancelText = "되돌리기",
                        onCancel = { undoApply(response.policyId) }
                    ))
                }
                .onFailure {
                    _toastEvent.value = Event(ToastInfo(it.message ?: "작업에 실패했습니다."))
                }
        }
    }

    // ✨ 5. API 호출 없이 로컬 데이터의 상태를 갱신하는 도우미 함수
    private fun updateLocalPolicyState(toggleResponse: ToggleApplyResponse) {
        val currentPolicies = _allPolicies.value?.toMutableList() ?: return
        val index = currentPolicies.indexOfFirst { it.policyId == toggleResponse.policyId }

        if (index != -1) {
            val updatedPolicy = currentPolicies[index].copy(applied = toggleResponse.applied)
            currentPolicies[index] = updatedPolicy

            // 전체 정책 리스트를 갱신하면, 이를 관찰하는 다른 LiveData들도 자동으로 업데이트됨
            _allPolicies.value = currentPolicies
            updateEventsMap(currentPolicies)
            updateSchedulesForDate(selectedDate.value!!, currentPolicies)
        }
    }
}