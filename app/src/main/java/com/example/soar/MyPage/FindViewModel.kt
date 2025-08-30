// MyPage/FindViewModel.kt
package com.example.soar.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.soar.EntryPage.SignUp.combineLatest
import com.example.soar.ArchivingPage.Event
import java.time.LocalDate

class FindViewModel : ViewModel() {

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

    val isSexValid: LiveData<Boolean> = sexDigit.map { it.length == 1 && it[0] in "12349" }

    /* ── ④ 모두 통과 여부 → 버튼 활성화 ──────────────────────────── */
    val canProceed: LiveData<Boolean> =
        listOf(isNameValid, isBirthValid, isSexValid)
            .combineLatest { list -> list.all { it } }



    val isIdButtonEnabled: LiveData<Boolean> =
        listOf(isNameValid, isBirthValid, isSexValid)
            .combineLatest { list -> list.all { it } }

    // --- 비밀번호 찾기(Find Password)용 LiveData (기존 코드 유지) ---
    val findPwName = MutableLiveData("")
    val findPwEmail = MutableLiveData("")

    val findPwNameTouched = MutableLiveData(false)
    val findPwEmailTouched = MutableLiveData(false)

    val isFindPwNameValid: LiveData<Boolean> = findPwName.map { it.matches(NAME_REGEX) }
    val isFindPwEmailValid: LiveData<Boolean> = findPwEmail.map { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }

    val isFindPwButtonEnabled: LiveData<Boolean> =
        listOf(isFindPwNameValid, isFindPwEmailValid)
            .combineLatest { list -> list.all { it } }

    // --- API 연동을 위한 결과 LiveData ---
    private val _findIdResult = MutableLiveData<Event<Unit>>()
    val findIdResult: LiveData<Event<Unit>> get() = _findIdResult

    private val _findPwResult = MutableLiveData<Event<String>>()
    val findPwResult: LiveData<Event<String>> get() = _findPwResult

    fun findId() {
        if (isIdButtonEnabled.value == true) {
            // TODO: 추후 API 연동 시, 여기서 Repository 호출
            _findIdResult.value = Event(Unit) // 성공 이벤트 발생
        }
    }

    fun findPassword() {
        if (isFindPwButtonEnabled.value == true) {
            // TODO: 추후 API 연동 시, 여기서 Repository 호출
            _findPwResult.value = Event("임시 비밀번호가 메일로 발송되었습니다.") // 성공 메시지 발생
        }
    }

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z\\s]{1,20}$")
    }
}