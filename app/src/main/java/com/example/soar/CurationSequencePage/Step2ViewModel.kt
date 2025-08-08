package com.example.soar.CurationSequencePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Step2ViewModel : ViewModel() {

    private val _allJobTags = MutableLiveData<List<TagResponse>>()

    // [변경] 여러 ID를 저장하던 Set을 하나의 ID만 저장하도록 Int? 타입으로 변경
    private val _selectedTagId = MutableLiveData<Int?>(null)
    val selectedTagId: LiveData<Int?> get() = _selectedTagId

    // UI에 표시될 키워드 리스트
    val jobTagsUiModel: LiveData<List<TagUiModel>> = MediatorLiveData<List<TagUiModel>>().apply {
        addSource(_allJobTags) { tags ->
            val selectedId = _selectedTagId.value
            value = tags.map { tag ->
                // [수정] TagUiModel 생성자에 4개의 인자를 올바른 순서로 전달합니다.
                // 3번째 인자로 tag.fieldId (Int)를 추가합니다.
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, tag.tagId == selectedId)
            }
        }
        addSource(_selectedTagId) { selectedId ->
            val tags = _allJobTags.value ?: return@addSource
            value = tags.map { tag ->
                // [수정] TagUiModel 생성자에 4개의 인자를 올바른 순서로 전달합니다.
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, tag.tagId == selectedId)
            }
        }
    }

    fun loadInitialTags(tags: List<TagResponse>) {
        _allJobTags.value = tags.filter { it.fieldId == 6 }
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