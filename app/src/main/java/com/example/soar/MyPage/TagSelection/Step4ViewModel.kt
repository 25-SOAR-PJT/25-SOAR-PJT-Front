package com.example.soar.MyPage.TagSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soar.Network.tag.TagResponse

// JSON 파싱과 UI 상태 관리를 위한 데이터 클래스

class Step4ViewModel : ViewModel() {

    // JSON에서 로드된 모든 '직업' 관련 태그 원본 리스트
    private val _allExtraTags = MutableLiveData<List<TagResponse>>()

    // 현재 선택된 태그들의 ID를 저장하는 Set
    private val _selectedTagIds = MutableLiveData<Set<Int>>(emptySet())
    val selectedTagIds: LiveData<Set<Int>> get() = _selectedTagIds

    // UI에 표시될 키워드 리스트. 원본 태그 리스트와 선택된 ID Set을 조합하여 생성됩니다.
    val extraTagsUiModel: LiveData<List<TagUiModel>> = MediatorLiveData<List<TagUiModel>>().apply {
        // 관찰할 소스 LiveData 추가
        addSource(_allExtraTags) { tags ->
            val selectedIds = _selectedTagIds.value ?: emptySet()
            // 원본 데이터가 변경되면 UI 모델 리스트 갱신
            value = tags.map { tag ->
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, selectedIds.contains(tag.tagId))
            }
        }
        addSource(_selectedTagIds) { selectedIds ->
            val tags = _allExtraTags.value ?: return@addSource
            // 선택된 ID Set이 변경되면 UI 모델 리스트 갱신
            value = tags.map { tag ->
                TagUiModel(tag.tagId, tag.tagName, tag.fieldId, selectedIds.contains(tag.tagId))
            }
        }
    }

    fun loadInitialTags(tags: List<TagResponse>) {
        _allExtraTags.value = tags.filter { it.fieldId == 7 }
    }

    /**
     * 사용자가 키워드를 클릭했을 때 호출되는 함수.
     * 선택된 태그 ID를 Set에 추가하거나 제거합니다.
     */
    fun toggleTagSelection(tagId: Int) {
        val currentSelected = _selectedTagIds.value?.toMutableSet() ?: mutableSetOf()
        if (currentSelected.contains(tagId)) {
            currentSelected.remove(tagId)
        } else {
            currentSelected.add(tagId)
        }
        _selectedTagIds.value = currentSelected
    }

    // [추가] 외부 데이터로 선택 상태를 초기화하는 함수
    fun initializeSelection(selectedIds: Set<Int>) {
        _selectedTagIds.value = selectedIds
    }
}