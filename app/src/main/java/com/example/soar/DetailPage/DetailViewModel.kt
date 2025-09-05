package com.example.soar.DetailPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.ArchivingPage.Event
import com.example.soar.Network.detail.DetailRepository
import com.example.soar.Network.detail.PolicyStepDetail
import com.example.soar.Network.detail.YouthPolicyDetail
import com.example.soar.Network.explore.ExploreRepository
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repository = DetailRepository()
    private val exploreRepository = ExploreRepository()

    private val _policyDetail = MutableLiveData<YouthPolicyDetail>()
    val policyDetail: LiveData<YouthPolicyDetail> = _policyDetail

    private val _policyStepDetail = MutableLiveData<PolicyStepDetail>()
    val policyStepDetail: LiveData<PolicyStepDetail> get() = _policyStepDetail

    // --- ✨ 북마크 관련 LiveData 추가 ---
    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _bookmarkEvent = MutableLiveData<Event<String>>()
    val bookmarkEvent: LiveData<Event<String>> = _bookmarkEvent

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadPolicyDetail(policyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getPolicyDetail(policyId)
                .onSuccess { detail ->
                    _policyDetail.postValue(detail)
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "정책 상세 정보를 불러오지 못했습니다.")
                }

            repository.getPolicyStepDetail(policyId)
                .onSuccess { stepDetail -> _policyStepDetail.postValue(stepDetail) }
                .onFailure { e -> _error.postValue(e.message ?: "신청 과정 정보를 불러오지 못했습니다.") }

            _isLoading.value = false
        }
    }

    /** ✨ 추가: policyId로 북마크 상태를 직접 조회하는 함수 */
    fun loadBookmarkStatus(policyId: String) {
        // 로그인 상태가 아니면 북마크를 false로 처리하고 함수 종료
        if (com.example.soar.Network.TokenManager.getAccessToken().isNullOrEmpty()) {
            _isBookmarked.postValue(false)
            return
        }

        viewModelScope.launch {
            repository.getBookmarkStatus(policyId)
                .onSuccess { isBookmarked ->
                    _isBookmarked.postValue(isBookmarked) // API 결과로 LiveData 업데이트
                }
                .onFailure { e ->
                    _error.postValue(e.message ?: "북마크 상태를 불러오지 못했습니다.")
                    _isBookmarked.postValue(false) // 실패 시 기본값 false로 설정
                }
        }
    }

    /** ✨ 2. 북마크 상태를 토글하는 API 호출 함수 */
    fun toggleBookmark(policyId: String) {
        viewModelScope.launch {
            exploreRepository.toggleBookmark(policyId)
                .onSuccess {
                    // API 호출 성공 시, 현재 북마크 상태를 반전시켜 LiveData 업데이트
                    val currentStatus = _isBookmarked.value ?: false
                    _isBookmarked.postValue(!currentStatus)
                    _bookmarkEvent.postValue(Event(if (!currentStatus) "북마크에 추가되었습니다." else "북마크가 해제되었습니다."))
                }
                .onFailure { e ->
                    // API 호출 실패 시, 에러 메시지 전달
                    _bookmarkEvent.postValue(Event(e.message ?: "오류가 발생했습니다."))
                }
        }
    }
}