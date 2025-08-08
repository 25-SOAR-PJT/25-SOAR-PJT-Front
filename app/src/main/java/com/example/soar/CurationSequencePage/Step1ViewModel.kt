package com.example.soar.CurationSequencePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class Step1ViewModel : ViewModel() {

    // [수정] 선택된 지역을 TagResponse 객체로 저장
    private val _location = MutableLiveData<TagResponse?>(null)
    val location: LiveData<TagResponse?> get() = _location

    // '다음' 버튼 활성화 여부
    val isNextEnabled: LiveData<Boolean> = _location.map { it != null }

    // [수정] 사용자가 지역을 선택했을 때 호출할 함수
    fun setLocation(selectedTag: TagResponse?) {
        _location.value = selectedTag
    }

    // 수정 모드 진입 시 상태 초기화를 위한 함수
    fun initializeSelection(selectedTag: TagResponse?) {
        _location.value = selectedTag
    }
}