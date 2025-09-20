// MyPage/FindViewModel.kt
package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.map
import com.example.soar.EntryPage.SignUp.combineLatest
import com.example.soar.ArchivingPage.Event
import kotlinx.coroutines.launch
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.user.FindIdResponse
import java.time.LocalDate

class FindViewModel : ViewModel() {

    // AuthRepository 인스턴스 추가
    private val authRepository = AuthRepository()

    // --- 아이디 찾기(Find ID)용 LiveData (통합 및 정리) ---
    val name = MutableLiveData("")
    val birth = MutableLiveData("")      // YYMMDD
    val sexDigit = MutableLiveData("")   // 1자리

    // 사용자가 각 필드를 터치했는지 여부
    val nameTouched = MutableLiveData(false)
    val birthTouched = MutableLiveData(false)
    val sexTouched = MutableLiveData(false)


    // 유효성 검사 로직 (기존 isFindId... -> is... 로 이름 변경)
    val isNameValid: LiveData<Boolean> = name.map { it.matches(NAME_REGEX) }


    /** 생년월일(YYMMDD) + 실존 날짜 체크 */
    val isBirthValid: LiveData<Boolean> = birth.map { txt ->
        if (txt.length != 6 || !txt.all(Char::isDigit)) return@map false
        try {
            val yy = txt.substring(0, 2).toInt()
            val mm = txt.substring(2, 4).toInt()
            val dd = txt.substring(4, 6).toInt()
            val century = if (yy > LocalDate.now().year % 100) 1900 else 2000
            LocalDate.of(century + yy, mm, dd)   // Exception → invalid
            true
        } catch (_: Exception) {
            false
        }
    }



    val isSexValid: LiveData<Boolean> = sexDigit.map { it.length == 1 && it[0] in "1234" }

    /* ── ④ 모두 통과 여부 → 버튼 활성화 ──────────────────────────── */
    val canProceed: LiveData<Boolean> =
        listOf(isNameValid, isBirthValid, isSexValid)
            .combineLatest { list -> list.all { it } }



    val isIdButtonEnabled: LiveData<Boolean> =
        listOf(isNameValid, isBirthValid, isSexValid)
            .combineLatest { list -> list.all { it } }

    // --- 비밀번호 찾기(Find Password)용 LiveData (기존 코드 유지) ---
    val namePw = MutableLiveData("")
    val email = MutableLiveData("")


    val namePwTouched = MutableLiveData(false)
    val emailTouched = MutableLiveData(false)

    val isNamePwValid: LiveData<Boolean> = namePw.map { it.matches(NAME_REGEX) }
    val isEmailValid: LiveData<Boolean> = email.map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }

    val isFindPwButtonEnabled: LiveData<Boolean> =
        listOf(isNamePwValid, isEmailValid)
            .combineLatest { list -> list.all { it } }

    // --- API 연동을 위한 결과 LiveData ---
    // 아이디 찾기: 성공 시 이메일 리스트, 실패 시 에러 메시지
    private val _findIdResult = MutableLiveData<Event<Result<FindIdResponse>>>()
    val findIdResult: LiveData<Event<Result<FindIdResponse>>> get() = _findIdResult

    private val _findPwResult = MutableLiveData<Event<String>>()
    val findPwResult: LiveData<Event<String>> get() = _findPwResult

    /** 아이디 찾기 API 호출 */
    fun findId() {
        if (isIdButtonEnabled.value != true) return

        viewModelScope.launch {
            val yymmdd = birth.value ?: return@launch
            val nameValue = name.value ?: return@launch

            // YYMMDD -> YYYY-MM-DD 변환
            val yy = yymmdd.substring(0, 2).toInt()
            val mm = yymmdd.substring(2, 4)
            val dd = yymmdd.substring(4, 6)
            val century = if (sexDigit.value in listOf("3", "4") || yy <= LocalDate.now().year % 100) 2000 else 1900
            val fullYear = century + yy
            val birthDate = "$fullYear-$mm-$dd"

            // Repository 호출
            val result = authRepository.findId(nameValue, birthDate)
            _findIdResult.value = Event(result)
        }
    }

    /** 비밀번호 찾기 API 호출 */
    fun findPassword() {
        // 버튼 활성화 상태가 아니면 함수 종료
        if (isFindPwButtonEnabled.value != true) return

        viewModelScope.launch {
            // LiveData에서 이메일과 이름 값 가져오기
            val emailValue = email.value ?: return@launch
            val namePwValue = namePw.value ?: return@launch

            // Repository 호출 후 결과를 LiveData에 저장
            authRepository.findPassword(emailValue, namePwValue)
                .onSuccess { message ->
                    _findPwResult.value = Event(message) // 성공 메시지 전달
                }
                .onFailure { error ->
                    _findPwResult.value = Event(error.message ?: "알 수 없는 오류가 발생했습니다.") // 실패 메시지 전달
                }
        }
    }

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z\\s]{1,20}$")
    }
}