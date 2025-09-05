package com.example.soar.ExplorePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.CurationSequencePage.Event
import com.example.soar.Network.explore.ExploreRepository
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.Network.tag.TagResponse
import com.example.soar.Network.user.UserTagRepository
import kotlinx.coroutines.launch


// ✨ 1. ViewModel에서 Activity로 전달할 화면 이동 이벤트를 정의
sealed class CurationNavigationEvent {
    object ProceedToCuration : CurationNavigationEvent()
    object ShowTermsAgreement : CurationNavigationEvent()
}

class PersonalBizViewModel(
    private val exploreRepository: ExploreRepository = ExploreRepository(),
    private val userTagRepository: UserTagRepository = UserTagRepository(),
    private val userRepository: AuthRepository = AuthRepository() // ✨ 2. UserRepository 주입
) : ViewModel() {

    private val _policies = MutableLiveData<List<YouthPolicy>>()
    val policies: LiveData<List<YouthPolicy>> = _policies

    private val _userTags = MutableLiveData<List<TagResponse>>()
    val userTags: LiveData<List<TagResponse>> = _userTags

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // ✨ 3. 화면 이동 이벤트를 전달할 LiveData 추가
    private val _navigationEvent = MutableLiveData<Event<CurationNavigationEvent>>()
    val navigationEvent: LiveData<Event<CurationNavigationEvent>> = _navigationEvent

    /**
     * ✨ 4. '태그 수정' 버튼 클릭 시 호출될 함수
     * 약관 동의 여부를 확인하고, 그 결과에 따라 내비게이션 이벤트를 발생시킴
     */
    fun onModifyTagsClicked() {
        viewModelScope.launch {
            userRepository.getTermAgreement()
                .onSuccess { hasAgreed ->
                    if (hasAgreed) {
                        // 이미 동의했다면, 큐레이션으로 바로 진행
                        _navigationEvent.value = Event(CurationNavigationEvent.ProceedToCuration)
                    } else {
                        // 동의하지 않았다면, 약관 동의 화면을 보여주도록 요청
                        _navigationEvent.value = Event(CurationNavigationEvent.ShowTermsAgreement)
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "약관 동의 정보를 확인하지 못했습니다."
                }
        }
    }



    /**
     * 사용자의 태그를 불러온 후, 해당 태그에 맞는 정책을 불러오는 함수
     */
    fun fetchPersonalPolicies() {
        if (_isLoading.value == true) return

        viewModelScope.launch {
            _isLoading.value = true

            // 1. 사용자 태그 불러오기
            val tagResult = userTagRepository.getUserTags()
            tagResult.onSuccess { userTagData ->
                val tags = userTagData
                _userTags.value = tags

                val tagIds = tags.joinToString(",") { it.tagId.toString() }

                // 2. 태그를 기반으로 정책 검색
                if (tagIds.isNotBlank()) {
                    exploreRepository.getMultiTagSearchPolicies(tagIds)
                        .onSuccess { response ->
                            _policies.value = response.content
                        }
                        .onFailure {
                            _policies.value = emptyList()
                            _error.value = it.message ?: "추천 정책을 불러오지 못했습니다."
                        }
                } else {
                    _policies.value = emptyList()
                    _error.value = "설정된 태그가 없습니다. 큐레이션을 진행해주세요."
                }
            }.onFailure {
                _userTags.value = emptyList()
                _policies.value = emptyList()
                _error.value = it.message ?: "사용자 태그를 불러오지 못했습니다."
            }

            _isLoading.value = false
        }
    }

    /**
     * 북마크 상태를 토글하고 API를 호출하는 함수
     */
    fun toggleBookmark(policy: YouthPolicy) {
        viewModelScope.launch {
            exploreRepository.toggleBookmark(policy.policyId).onSuccess {
                // 성공 시 LiveData의 정책 목록을 직접 업데이트
                val currentList = _policies.value?.toMutableList() ?: return@onSuccess
                val index = currentList.indexOfFirst { it.policyId == policy.policyId }

                if (index != -1) {
                    val updatedPolicy = policy.copy(bookmarked = !(policy.bookmarked ?: false))
                    currentList[index] = updatedPolicy
                    _policies.postValue(currentList)
                }
            }.onFailure { e ->
                _error.postValue(e.message ?: "북마크 상태 변경에 실패했습니다.")
            }
        }
    }
}