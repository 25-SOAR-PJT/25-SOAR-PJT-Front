package com.example.soar.ArchivingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soar.Business

class TagViewModel : ViewModel() {

    // ViewModel이 직접 데이터를 소유합니다.
    private val originalDataList = mutableListOf<Business>()
    private val _dataList = MutableLiveData<List<Business>>()
    val dataList: LiveData<List<Business>> = _dataList

    // 더미 데이터를 ViewModel에 로드하는 함수
    fun setInitialData(initialData: List<Business>) {
        if (originalDataList.isEmpty()) {
            originalDataList.addAll(initialData)
            _dataList.value = originalDataList
        }
    }

    // 데이터를 필터링하는 함수
    fun filterDataByTagIds(tagIds: List<Int>) {
        val filtered = originalDataList.filter { business ->
            business.tags.any { tag -> tagIds.contains(tag.tagId) }
        }
        _dataList.value = filtered
    }

    // 필터링을 초기화하고 전체 데이터를 복원하는 함수
    fun resetFilteredData() {
        _dataList.value = originalDataList
    }
}