package com.example.soar.CurationSequencePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Step3ViewModel : ViewModel() {

    private val _allEducationTags = MutableLiveData<List<TagResponse>>()

    // [변경] 여러 ID를 저장하던 Set을 하나의 ID만 저장하도록 Int? 타입으로 변경
    private val _selectedTagId = MutableLiveData<Int?>(null)
    val selectedTagId: LiveData<Int?> get() = _selectedTagId

    // UI에 표시될 키워드 리스트
    val educationTagsUiModel: LiveData<List<TagUiModel>> = MediatorLiveData<List<TagUiModel>>().apply {
        addSource(_allEducationTags) { tags ->
            // [변경] 단일 선택된 ID를 가져옴
            val selectedId = _selectedTagId.value
            value = tags.map { tag ->
                // [변경] 태그의 ID가 선택된 ID와 일치하는지 비교
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, tag.tagId == selectedId)
            }
        }
        addSource(_selectedTagId) { selectedId ->
            val tags = _allEducationTags.value ?: return@addSource
            // [변경] 선택된 ID가 변경되면 리스트 갱신
            value = tags.map { tag ->
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, tag.tagId == selectedId)
            }
        }
    }

    fun loadInitialTags(tags: List<TagResponse>) {
        _allEducationTags.value = tags.filter { it.fieldId == 9 }
    }

    /**
     * [이름 및 로직 변경] 사용자가 키워드를 클릭했을 때 호출되는 함수.
     * 단일 선택 로직으로 수정합니다.
     */
    fun selectTag(tagId: Int) {
        // 이미 선택된 태그를 다시 클릭하면 선택 해제, 다른 태그를 클릭하면 선택 변경
        if (_selectedTagId.value == tagId) {
            _selectedTagId.value = null
        } else {
            _selectedTagId.value = tagId
        }
    }

    // [추가] 외부 데이터로 선택 상태를 초기화하는 함수
    fun initializeSelection(selectedId: Int?) {
        _selectedTagId.value = selectedId
    }
}