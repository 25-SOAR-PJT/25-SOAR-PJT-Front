// com.example.soar.ArchivingPage/KeywordViewModel.kt

package com.example.soar.ArchivingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.CurationSequencePage.Event
import com.example.soar.CurationSequencePage.TagUiModel
import com.example.soar.Network.tag.TagRepository
import com.example.soar.Network.tag.TagResponse
import kotlinx.coroutines.launch

class KeywordViewModel : ViewModel() {

    private val repository = TagRepository()

    // 네트워크에서 로드된 원본 태그 리스트
    private val _allTags = MutableLiveData<List<TagResponse>>()

    // 현재 선택된 태그 ID Set
    private val _selectedTagIds = MutableLiveData<Set<Int>>(emptySet())
    val selectedTagIds: LiveData<Set<Int>> get() = _selectedTagIds

    // Toast 메시지 이벤트
    private val _showToast = MutableLiveData<Event<String>>()
    val showToast: LiveData<Event<String>> get() = _showToast

    // UI에 표시될 최종 모델 리스트 (원본 데이터 + 선택 상태 조합)
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

    /**
     * 네트워크를 통해 태그 데이터를 로드하는 함수.
     */
    fun loadTags() {
        // 데이터가 이미 로드되었다면 다시 호출하지 않음
        if (_allTags.value != null) return

        viewModelScope.launch {
            repository.getTags()
                .onSuccess { tags ->
                    // 조건에 맞게 필터링: fieldId < 5, tagName != "기타"
                    _allTags.value = tags.filter { it.fieldId < 5 && it.tagName != "기타" }
                }
                .onFailure {
                    // 에러 처리 (예: Toast 메시지 표시)
                    _showToast.value = Event("태그를 불러오는데 실패했습니다.")
                }
        }
    }

    /**
     * 태그 선택/해제 로직 (최대 5개 제한)
     */
    fun toggleTagSelection(tagId: Int) {
        val currentSelected = _selectedTagIds.value?.toMutableSet() ?: mutableSetOf()

        if (currentSelected.contains(tagId)) {
            currentSelected.remove(tagId)
        } else {
            if (currentSelected.size < 5) {
                currentSelected.add(tagId)
            } else {
                _showToast.value = Event("태그는 최대 5개까지 선택할 수 있습니다.")
            }
        }
        _selectedTagIds.value = currentSelected
    }

    /**
     * 외부 데이터로 선택 상태를 초기화하는 함수
     */
    fun initializeSelection(selectedIds: Set<Int>) {
        _selectedTagIds.value = selectedIds
    }

    /**
     * 모든 선택을 초기화하는 함수
     */
    fun resetSelection() {
        _selectedTagIds.value = emptySet()
    }
}