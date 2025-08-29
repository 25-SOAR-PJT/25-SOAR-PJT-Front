// 파일 경로: com/example/soar/HomePage/HomeViewModel.kt
package com.example.soar.HomePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.user.UserInfoResponse
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // UI에 표시될 사용자 정보
    private val _userInfo = MutableLiveData<UserInfoResponse>()
    val userInfo: LiveData<UserInfoResponse> get() = _userInfo

    // 에러 메시지
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    /**
     * 서버에서 최신 사용자 정보를 가져오는 함수
     */
    fun fetchUserInfo() {
        // ViewModel이 살아있는 동안만 코루틴 실행
        viewModelScope.launch {
            authRepository.getUserInfo()
                .onSuccess { fetchedUserInfo ->
                    // 성공 시 LiveData 업데이트
                    _userInfo.value = fetchedUserInfo
                }
                .onFailure { throwable ->
                    // 실패 시 에러 LiveData 업데이트
                    _error.value = throwable.message
                }
        }
    }
}