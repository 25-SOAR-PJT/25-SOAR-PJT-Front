package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.ArchivingPage.Event
import com.example.soar.Network.tag.TagResponse
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.user.UserDetailInfoResponse
import com.example.soar.Network.user.UserTagRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userTagRepository: UserTagRepository = UserTagRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _userTags = MutableLiveData<List<TagResponse>>()
    val userTags: LiveData<List<TagResponse>> get() = _userTags

    private val _userDetailInfo = MutableLiveData<UserDetailInfoResponse>()
    val userDetailInfo: LiveData<UserDetailInfoResponse> get() = _userDetailInfo

    // ✨ 추가: 정보 수정 성공/실패 이벤트를 처리하기 위한 LiveData
    private val _updateEvent = MutableLiveData<Event<String>>()
    val updateEvent: LiveData<Event<String>> get() = _updateEvent

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _fetchError = MutableLiveData<String>()
    val fetchError: LiveData<String> get() = _fetchError

    // ✨ 화면에 필요한 모든 데이터를 가져오는 통합 함수
    fun fetchProfileData() {
        // 이미 로딩 중이면 중복 실행 방지
        if (_isLoading.value == true) return
        _isLoading.value = true

        viewModelScope.launch {
            // 두 API 호출을 각각의 Job으로 동시에 시작
            val detailInfoJob = launch { fetchUserDetailInfo() }
            val userTagsJob = launch { fetchUserTags() }

            // 두 Job이 모두 끝날 때까지 기다림
            joinAll(detailInfoJob, userTagsJob)

            // 모든 작업이 끝나면 로딩 상태를 false로 변경
            _isLoading.value = false
        }
    }

    // ✨ private으로 변경하여 외부에서 직접 호출하지 않도록 함
    private suspend fun fetchUserDetailInfo() {
        authRepository.getUserDetailInfo()
            .onSuccess { _userDetailInfo.postValue(it) } // 백그라운드 스레드이므로 postValue 사용
            .onFailure { _fetchError.postValue(it.message) }
    }

    // ✨ private으로 변경하고 isLoading 관리는 상위 함수로 이전
    private suspend fun fetchUserTags() {
        userTagRepository.getUserTags()
            .onSuccess { tags -> _userTags.postValue(tags) }
            .onFailure { _fetchError.postValue(it.message) }
    }
    // ✨ 추가: 이름 변경 요청 함수
    fun updateUserName(newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.updateUserName(newName)
                .onSuccess { successMessage ->
                    _updateEvent.value = Event(successMessage)
                }
                .onFailure {
                    _fetchError.value = it.message ?: "이름 변경에 실패했습니다."
                }
            _isLoading.value = false
        }
    }

    // ✨ 추가: 생년월일 변경 요청 함수
    fun updateUserBirth(year: String, month: String, day: String) {
        // 날짜 형식 검증 및 변환 ("YYYY-MM-DD")
        val formattedDate = try {
            String.format("%s-%02d-%02d", year, month.toInt(), day.toInt())
        } catch (e: Exception) {
            _fetchError.value = "올바른 날짜 형식이 아닙니다."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            authRepository.updateUserBirth(formattedDate)
                .onSuccess { successMessage ->
                    _updateEvent.value = Event(successMessage)
                }
                .onFailure {
                    _fetchError.value = it.message ?: "생년월일 변경에 실패했습니다."
                }
            _isLoading.value = false
        }
    }

    fun updateUserGender(newGender: Boolean?) {
        // '미설정'은 백엔드에서 별도 처리가 필요할 수 있습니다.
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.updateUserGender(newGender)
                .onSuccess { successMessage ->
                    _updateEvent.value = Event(successMessage)
                }
                .onFailure {
                    _fetchError.value = it.message ?: "성별 변경에 실패했습니다."
                }
            _isLoading.value = false
        }
    }

    fun saveProfileChanges(newName: String?, newBirthDate: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val updateTasks = mutableListOf<Deferred<Result<String>>>()

            // 이름이 변경되었으면 작업 목록에 추가
            newName?.let {
                val nameTask = async { authRepository.updateUserName(it) }
                updateTasks.add(nameTask)
            }

            // 생년월일이 변경되었으면 작업 목록에 추가
            newBirthDate?.let {
                val birthTask = async { authRepository.updateUserBirth(it) }
                updateTasks.add(birthTask)
            }

            // 모든 작업을 병렬로 실행하고 결과를 기다림
            val results = updateTasks.awaitAll()

            // 모든 결과가 성공했는지 확인
            if (results.all { it.isSuccess }) {
                _updateEvent.value = Event("프로필이 성공적으로 저장되었습니다.")
            } else {
                // 실패한 작업의 오류 메시지를 찾아서 표시
                val firstError = results.firstOrNull { it.isFailure }?.exceptionOrNull()
                _fetchError.value = firstError?.message ?: "프로필 저장에 실패했습니다."
            }
            _isLoading.value = false
        }
    }
}