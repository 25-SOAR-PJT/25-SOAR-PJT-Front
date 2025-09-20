package com.example.soar.CurationSequencePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.explore.ExploreRepository
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.Network.tag.TagRepository
import com.example.soar.Network.tag.TagResponse
import kotlinx.coroutines.launch

class CurationSequenceViewModel : ViewModel() {

    // [수정] tagRepository와 exploreRepository 모두 사용
    private val tagRepository = TagRepository()
    private val exploreRepository = ExploreRepository()

    // ✨추가: 추천 정책 데이터를 저장할 LiveData
    private val _suggestedPolicies = MutableLiveData<List<YouthPolicy>>()
    val suggestedPolicies: LiveData<List<YouthPolicy>> get() = _suggestedPolicies

    // ✨추가: 로딩 상태를 관리할 LiveData
    private val _isLoadingPolicies = MutableLiveData<Boolean>(false)
    val isLoadingPolicies: LiveData<Boolean> get() = _isLoadingPolicies

    // Step 0: 사용자 이름
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName
    fun setUserName(name: String) {
        _userName.value = name
    }

    // Step 1: 지역 (단일 선택)
    private val _location = MutableLiveData<TagResponse?>()
    val location: LiveData<TagResponse?> get() = _location
    fun setLocation(tag: TagResponse?) {
        _location.value = tag
    }

    // Step 2: 직업 (단일 선택)
    private val _job = MutableLiveData<TagResponse?>()
    val job: LiveData<TagResponse?> get() = _job
    fun setJob(tag: TagResponse?) {
        _job.value = tag
    }

    // Step 3: 학력 (단일 선택)
    private val _education = MutableLiveData<TagResponse?>()
    val education: LiveData<TagResponse?> get() = _education
    fun setEducation(tag: TagResponse?) {
        _education.value = tag
    }

    // Step 4: 추가 조건 (다중 선택)
    private val _additionalConditions = MutableLiveData<List<TagResponse>>()
    val additionalConditions: LiveData<List<TagResponse>> get() = _additionalConditions
    fun setAdditionalConditions(tags: List<TagResponse>) {
        _additionalConditions.value = tags
    }

    // Step 5: 관심 키워드 (다중 선택)
    private val _keywords = MutableLiveData<List<TagResponse>>()
    val keywords: LiveData<List<TagResponse>> get() = _keywords
    fun setKeywords(tags: List<TagResponse>) {
        _keywords.value = tags
    }

    // [추가] 수정 모드 상태를 관리하기 위한 LiveData
    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> get() = _isEditMode

    fun setEditMode(isEditing: Boolean) {
        _isEditMode.value = isEditing
    }

    // [수정] 모든 태그 데이터를 저장할 LiveData (기존 코드 유지)
    private val _allTags = MutableLiveData<List<TagResponse>>()
    val allTags: LiveData<List<TagResponse>> get() = _allTags

    /**
     * [수정] 네트워크에서 모든 태그 데이터를 로드하는 함수.
     * Coroutine을 사용하여 비동기 처리합니다.
     */
    fun loadAllTags() {
        if (_allTags.value != null) {
            return
        } // 이미 데이터가 있으면 다시 로드하지 않음

        // viewModelScope를 사용하여 ViewModel 생명주기에 안전한 코루틴 실행
        viewModelScope.launch {
            tagRepository.getTags()
                .onSuccess { tags ->
                    // 성공 시 LiveData에 태그 리스트 할당
                    _allTags.value = tags
                }
                .onFailure {
                    // 실패 시 에러 로그 출력 (추후 에러 상태 LiveData를 만들어 UI에 표시 가능)
                    it.printStackTrace()
                }
        }
    }

    /**
     * ✨추가: ViewModel에 저장된 모든 선택된 태그들을 결합하여 문자열로 반환하는 함수✨
     */
    private fun getCombinedKeywords(): String {
        val selectedTags = mutableListOf<TagResponse>()

        // 단일 선택 태그들
        location.value?.let { selectedTags.add(it) }
        job.value?.let { selectedTags.add(it) }
        education.value?.let { selectedTags.add(it) }

        // 다중 선택 태그 리스트들
        additionalConditions.value?.let { selectedTags.addAll(it) }
        keywords.value?.let { selectedTags.addAll(it) }

        return selectedTags.joinToString(",") { it.tagName }
    }

    // ✨수정: 선택된 태그의 ID를 결합하는 함수
    private fun getCombinedTagIds(): String {
        val selectedTags = mutableListOf<TagResponse>()

        // 단일 선택 태그들
        location.value?.let { selectedTags.add(it) }
        job.value?.let { selectedTags.add(it) }
        education.value?.let { selectedTags.add(it) }

        // 다중 선택 태그 리스트들
        additionalConditions.value?.let { selectedTags.addAll(it) }
        keywords.value?.let { selectedTags.addAll(it) }

        return selectedTags.joinToString(",") { it.tagId.toString() }
    }

    /**
     * ✨추가: 서버에서 추천 정책 리스트를 가져오는 함수✨
     */
    fun fetchSuggestedPolicies() {
        if (_isLoadingPolicies.value == true) return // 이미 로딩 중이면 중복 호출 방지

        val tags = getCombinedTagIds()
        if (tags.isBlank()) {
            _suggestedPolicies.value = emptyList()
            return
        }

        _isLoadingPolicies.value = true
        viewModelScope.launch {
            exploreRepository.getMultiTagSearchPolicies(tags)
                .onSuccess { response ->
                    _suggestedPolicies.value = response.content
                }
                .onFailure {
                    it.printStackTrace()
                    _suggestedPolicies.value = emptyList()
                }
            _isLoadingPolicies.value = false
        }
    }
}