package com.example.soar.Utill

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soar.CurationSequencePage.Event // Event Wrapper 클래스는 다른 패키지에 있을 수 있음
import com.example.soar.Network.user.AuthRepository
import kotlinx.coroutines.launch

class TermAgreeViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // API 호출 결과를 Activity에 알리기 위한 LiveData (성공/실패 여부)
    private val _agreementResult = MutableLiveData<Event<Boolean>>()
    val agreementResult: LiveData<Event<Boolean>> get() = _agreementResult

    // 에러 메시지를 전달하기 위한 LiveData
    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> get() = _error

    /**
     * '민감정보 처리 동의' API를 호출하는 함수
     */
    fun agreeToTerm() {
        viewModelScope.launch {
            authRepository.agreeToOptionalTerm()
                .onSuccess {
                    // API 호출 성공 시, true 이벤트를 전달
                    _agreementResult.value = Event(true)
                }
                .onFailure {
                    // API 호출 실패 시, 에러 메시지와 함께 false 이벤트를 전달
                    _error.value = Event(it.message ?: "약관 동의 처리 중 오류가 발생했습니다.")
                    _agreementResult.value = Event(false)
                }
        }
    }
}