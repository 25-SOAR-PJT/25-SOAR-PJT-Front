// com.example.soar.ExplorePage/ExploreViewModel.kt

package com.example.soar.ExplorePage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.explore.ExploreRepository
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.Network.tag.TagRepository
import com.example.soar.Network.tag.TagResponse
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val policyRepository = ExploreRepository()
    private val tagRepository = TagRepository()

    private val _policies = MutableLiveData<List<YouthPolicy>>()
    val policies: LiveData<List<YouthPolicy>> = _policies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


    private val _allTags = MutableLiveData<List<TagResponse>>()
    private val _selectedKeywords = MutableLiveData<List<TagResponse>>(emptyList())
    val selectedKeywords: LiveData<List<TagResponse>> = _selectedKeywords

    private var initialTagIds: List<Int>? = null

    private var currentPage = 0
    private var isLastPage = false
    private var isDataLoading = false
    private var currentCategory: String? = null

    init {
        loadAllTags()
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            tagRepository.getTags().onSuccess { allTagsList ->
                _allTags.value = allTagsList

                // [추가] 전체 태그 로드가 완료된 후, 임시 저장된 ID가 있다면 키워드 선택 실행
                initialTagIds?.let { ids ->
                    updateKeywordsByIds(ids)
                    initialTagIds = null // 처리 후 초기화
                }
            }
        }
    }

    fun loadPolicies(category: String? = null, isNewQuery: Boolean = false) {
        val keywordQuery = _selectedKeywords.value?.joinToString(",") { it.tagName }
        val tagIdQuery = _selectedKeywords.value?.joinToString(",") { it.tagId.toString() }

        Log.d(
            "Paging",
            "call: page=$currentPage, isLast=$isLastPage, loading=$isDataLoading, new=$isNewQuery"
        )

        if (isDataLoading || (isLastPage && !isNewQuery)) return

        viewModelScope.launch {
            isDataLoading = true

            // ✨수정된 부분: 새 쿼리일 때만 로딩 상태를 'true'로 설정✨
            if (isNewQuery) {
                _isLoading.postValue(true)
                currentPage = 0
                isLastPage = false
                _policies.value = emptyList()
                currentCategory = category
            }

            policyRepository.getPolicies(currentCategory, tagIdQuery, currentPage)
                .onSuccess { response ->
                    val currentList = _policies.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(response.content)
                    _policies.postValue(currentList)

                    isLastPage = response.isLast
                    if (!isLastPage) {
                        currentPage++
                    }
                }.onFailure { e ->
                    _error.postValue(e.message ?: "알 수 없는 오류가 발생했습니다.")
                }

            _isLoading.postValue(false)
            isDataLoading = false
        }
        Log.d("Paging", "done: nextPage=$currentPage, isLast=$isLastPage")
    }

    /**
     * ✨추가: 북마크 상태를 토글하고 API를 호출하는 함수✨
     */
    fun toggleBookmark(policy: YouthPolicy) {
        viewModelScope.launch {
            policyRepository.toggleBookmark(policy.policyId).onSuccess { message ->
                // 성공 시, LiveData의 목록을 직접 업데이트
                val currentList = _policies.value?.toMutableList() ?: return@onSuccess
                val index = currentList.indexOfFirst { it.policyId == policy.policyId }

                if (index != -1) {
                    // 북마크 상태를 토글한 새로운 객체 생성
                    val updatedPolicy = policy.copy(bookmarked = !(policy.bookmarked ?: false))
                    currentList[index] = updatedPolicy

                    // LiveData에 새 목록을 post하여 UI 업데이트
                    _policies.postValue(currentList)
                }

                // 성공 메시지 전달 (선택 사항)
                // Toast 메시지 등 UI 업데이트는 Fragment에서 처리하는 것이 좋음
                Log.d("BookmarkToggle", message)
            }.onFailure { e ->
                // 실패 시 에러 메시지 전달
                _error.postValue(e.message ?: "북마크 상태 변경에 실패했습니다.")
            }
        }
    }

    fun updateKeywordsByIds(selectedIds: List<Int>) {
        val allTagsList = _allTags.value

        // [수정] 전체 태그 목록이 로드되었는지 확인
        if (allTagsList.isNullOrEmpty()) {
            // 아직 로드되지 않았다면, 전달받은 ID를 임시 변수에 저장
            initialTagIds = selectedIds
        } else {
            // 태그 목록이 있으면, 정상적으로 필터링하여 LiveData 업데이트
            val selectedTags = allTagsList.filter { it.tagId in selectedIds }
            _selectedKeywords.value = selectedTags
        }
    }
    fun updatePoliciesBookmarks(changes: HashMap<String, Boolean>) {
        val currentList = _policies.value?.toMutableList() ?: return
        var listModified = false

        changes.forEach { (policyId, isBookmarked) ->
            val index = currentList.indexOfFirst { it.policyId == policyId }
            if (index != -1) {
                val policyToUpdate = currentList[index]
                if (policyToUpdate.bookmarked != isBookmarked) {
                    currentList[index] = policyToUpdate.copy(bookmarked = isBookmarked)
                    listModified = true
                }
            }
        }

        // 변경 사항이 하나라도 있을 경우에만 LiveData를 한 번 업데이트
        if (listModified) {
            _policies.postValue(currentList)
        }
    }
}
