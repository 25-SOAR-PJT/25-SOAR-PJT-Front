package com.example.soar.MyPage.TagSelection


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.TokenManager
import com.example.soar.Network.tag.TagRepository
import com.example.soar.Network.tag.TagResponse
import com.example.soar.Network.user.UserTagRepository
import kotlinx.coroutines.launch

class TagSelectionViewModel : ViewModel() {

    private val tagRepository = TagRepository()
    private val userTagRepository = UserTagRepository()

    private val _location = MutableLiveData<TagResponse?>()
    val location: LiveData<TagResponse?> get() = _location
    fun setLocation(tag: TagResponse?) {
        _location.value = tag
    }

    private val _job = MutableLiveData<TagResponse?>()
    val job: LiveData<TagResponse?> get() = _job
    fun setJob(tag: TagResponse?) {
        _job.value = tag
    }

    private val _education = MutableLiveData<TagResponse?>()
    val education: LiveData<TagResponse?> get() = _education
    fun setEducation(tag: TagResponse?) {
        _education.value = tag
    }

    private val _additionalConditions = MutableLiveData<List<TagResponse>>()
    val additionalConditions: LiveData<List<TagResponse>> get() = _additionalConditions
    fun setAdditionalConditions(tags: List<TagResponse>) {
        _additionalConditions.value = tags
    }

    private val _keywords = MutableLiveData<List<TagResponse>>()
    val keywords: LiveData<List<TagResponse>> get() = _keywords
    fun setKeywords(tags: List<TagResponse>) {
        _keywords.value = tags
    }

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> get() = _isEditMode

    fun setEditMode(isEditing: Boolean) {
        _isEditMode.value = isEditing
    }

    private val _allTags = MutableLiveData<List<TagResponse>>()
    val allTags: LiveData<List<TagResponse>> get() = _allTags

    fun loadAllTags() {
        if (_allTags.value != null) {
            return
        }

        viewModelScope.launch {
            tagRepository.getTags()
                .onSuccess { tags ->
                    _allTags.value = tags
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
    }

    // ✨ 추가: 태그 저장 성공을 알리는 이벤트 LiveData
    private val _saveSuccessEvent = MutableLiveData<Event<Unit>>()
    val saveSuccessEvent: LiveData<Event<Unit>> get() = _saveSuccessEvent


    // ✨ 추가: 선택된 태그 ID들을 조합하여 서버에 전송하는 함수
    fun saveUserTags() {
        viewModelScope.launch {
            val selectedTagIds = getCombinedTagIds()
                .split(",")
                .filter { it.isNotBlank() && it.toIntOrNull() != 0 } // ✨ 수정: tagId가 0이 아닌 경우만 필터링
                .map { it.toInt() }

            // 로그 추가: 전송될 실제 태그 목록 확인
            Log.d("TagSelectionViewModel", "전송할 태그 목록: $selectedTagIds")

            userTagRepository.modifyUserTags(selectedTagIds)
                .onSuccess { tags ->
                    Log.d("TagSelectionViewModel", "태그 저장 성공: $tags")

                    val newLocationTag = tags.find { it.fieldId == TagSelectionActivity.STEP1_FIELD_ID }
                    if (newLocationTag != null) {
                        val userInfo = TokenManager.getUserInfo()
                        userInfo?.let {
                            val updatedUserInfo = it.copy(userAddress = newLocationTag.tagName)
                            TokenManager.saveUserInfo(updatedUserInfo)
                        }
                    }
                    _saveSuccessEvent.value = Event(Unit) // ✨ 추가: 성공 이벤트 발생
                }
                .onFailure {
                    Log.e("TagSelectionViewModel", "태그 저장 실패: ${it.message}")
                    // 실패 이벤트 처리 로직 추가 (필요시)
                }
        }
    }

    private fun getCombinedTagIds(): String {
        val selectedTags = mutableListOf<TagResponse>()

        location.value?.let { selectedTags.add(it) }
        job.value?.let { selectedTags.add(it) }
        education.value?.let { selectedTags.add(it) }

        additionalConditions.value?.let { selectedTags.addAll(it) }
        keywords.value?.let { selectedTags.addAll(it) }

        return selectedTags.joinToString(",") { it.tagId.toString() }
    }


    /**
     * ✨추가: 프로필 화면에서 전달된 태그 데이터로 ViewModel 상태를 초기화하는 함수✨
     */
    fun populateFromUserTags(tags: List<TagResponse>) {
        // 기존 선택 항목을 초기화
        setLocation(null)
        setJob(null)
        setEducation(null)
        setAdditionalConditions(emptyList())
        setKeywords(emptyList())

        val groupedTags = tags.groupBy { it.fieldId }

        // 단일 선택 태그 초기화
        groupedTags[9]?.firstOrNull()?.let { setLocation(it) }
        groupedTags[5]?.firstOrNull()?.let { setJob(it) }
        groupedTags[8]?.firstOrNull()?.let { setEducation(it) }

        // 다중 선택 태그 초기화
        groupedTags[7]?.let { setAdditionalConditions(it) }

        // 관심 키워드 통합 및 초기화
        val keywordTags = listOfNotNull(
            groupedTags[1],
            groupedTags[2],
            groupedTags[3],
            groupedTags[4]
        ).flatten()
        setKeywords(keywordTags)
    }
}