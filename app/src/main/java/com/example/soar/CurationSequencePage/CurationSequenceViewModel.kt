package com.example.soar.CurationSequencePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurationSequenceViewModel : ViewModel() {

    // Step 0: 사용자 이름
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName
    fun setUserName(name: String) { _userName.value = name }

    // Step 1: 지역 (단일 선택)
    private val _location = MutableLiveData<TagResponse?>()
    val location: LiveData<TagResponse?> get() = _location
    fun setLocation(tag: TagResponse?) { _location.value = tag }

    // Step 2: 직업 (단일 선택)
    private val _job = MutableLiveData<TagResponse?>()
    val job: LiveData<TagResponse?> get() = _job
    fun setJob(tag: TagResponse?) { _job.value = tag }

    // Step 3: 학력 (단일 선택)
    private val _education = MutableLiveData<TagResponse?>()
    val education: LiveData<TagResponse?> get() = _education
    fun setEducation(tag: TagResponse?) { _education.value = tag }

    // Step 4: 추가 조건 (다중 선택)
    private val _additionalConditions = MutableLiveData<List<TagResponse>>()
    val additionalConditions: LiveData<List<TagResponse>> get() = _additionalConditions
    fun setAdditionalConditions(tags: List<TagResponse>) { _additionalConditions.value = tags }

    // Step 5: 관심 키워드 (다중 선택)
    private val _keywords = MutableLiveData<List<TagResponse>>()
    val keywords: LiveData<List<TagResponse>> get() = _keywords
    fun setKeywords(tags: List<TagResponse>) { _keywords.value = tags }

    // [추가] 수정 모드 상태를 관리하기 위한 LiveData
    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> get() = _isEditMode

    fun setEditMode(isEditing: Boolean) {
        _isEditMode.value = isEditing
    }
}