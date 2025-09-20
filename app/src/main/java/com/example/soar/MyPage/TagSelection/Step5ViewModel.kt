package com.example.soar.MyPage.TagSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soar.Network.tag.TagResponse

class Step5ViewModel : ViewModel() {

    // JSON에서 로드된 모든 태그 원본 리스트
    private val _allTags = MutableLiveData<List<TagResponse>>()

    // 현재 선택된 태그들의 ID를 저장하는 Set
    private val _selectedTagIds = MutableLiveData<Set<Int>>(emptySet())
    val selectedTagIds: LiveData<Set<Int>> get() = _selectedTagIds

    // [추가] Toast 메시지를 위한 LiveData
    private val _showToast = MutableLiveData<Event<String>>()
    val showToast: LiveData<Event<String>> get() = _showToast

    // UI에 표시될 전체 키워드 리스트
    val tagsUiModel: LiveData<List<TagUiModel>> = MediatorLiveData<List<TagUiModel>>().apply {
        addSource(_allTags) { tags ->
            val selectedIds = _selectedTagIds.value ?: emptySet()
            value = tags.map { tag ->
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, selectedIds.contains(tag.tagId))
            }
        }
        addSource(_selectedTagIds) { selectedIds ->
            val tags = _allTags.value ?: return@addSource
            value = tags.map { tag ->
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, selectedIds.contains(tag.tagId))
            }
        }
    }

    fun loadInitialTags(tags: List<TagResponse>) {
        _allTags.value = tags.filter { it.fieldId < 5 && it.tagName != "기타" }
    }

    // [수정] 태그 선택/해제 로직에 최대 5개 선택 제한 기능 추가
    fun toggleTagSelection(tagId: Int) {
        val currentSelected = _selectedTagIds.value?.toMutableSet() ?: mutableSetOf()

        if (currentSelected.contains(tagId)) {
            // 이미 선택된 태그를 해제하는 경우: 항상 허용
            currentSelected.remove(tagId)
        } else {
            // 새로운 태그를 선택하는 경우: 개수 확인
            if (currentSelected.size < 5) {
                currentSelected.add(tagId)
            } else {
                // 이미 5개가 선택되어 있을 경우 Toast 메시지 이벤트 발생
                _showToast.value = Event("태그는 최대 5개까지 선택할 수 있습니다.")
            }
        }
        _selectedTagIds.value = currentSelected
    }

    // [추가] 외부 데이터로 선택 상태를 초기화하는 함수
    fun initializeSelection(selectedIds: Set<Int>) {
        _selectedTagIds.value = selectedIds
    }
}