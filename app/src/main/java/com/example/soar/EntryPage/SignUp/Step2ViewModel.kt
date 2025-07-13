package com.example.soar.EntryPage.SignUp

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 네트워크 결과 */
sealed interface VerifyState {
    object Idle : VerifyState
    object Loading : VerifyState
    object Success : VerifyState
    data class Error(val msg: String) : VerifyState
}

class Step2ViewModel(
    private val handle: SavedStateHandle           // ← 화면 회전 시 값 보존
) : ViewModel() {

    /* ── ① 입력값 ─────────────────────────────────────────────────── */
    val name     = handle.getLiveData("name"    , "")
    val birth    = handle.getLiveData("birth"   , "")   // YYMMDD
    val sexDigit = handle.getLiveData("sexDigit", "")   // 1자리

    /* ── ② touched flag ──────────────────────────────────────────── */
    val nameTouched  = handle.getLiveData("nameTouched" , false)
    val birthTouched = handle.getLiveData("birthTouched", false)
    val sexTouched   = handle.getLiveData("sexTouched"  , false)

    /* ── ③ 개별 유효성 ───────────────────────────────────────────── */
    val isNameValid : LiveData<Boolean> =
        name.map { it.matches(NAME_REGEX) }

    /** 생년월일(YYMMDD) + 실존 날짜 체크 */
    val isBirthValid : LiveData<Boolean> = birth.map { txt ->
        if (txt.length != 6 || !txt.all(Char::isDigit)) return@map false
        try {
            val yy = txt.substring(0, 2).toInt()
            val mm = txt.substring(2, 4).toInt()
            val dd = txt.substring(4, 6).toInt()
            val century = if (yy > LocalDate.now().year % 100) 1900 else 2000
            LocalDate.of(century + yy, mm, dd)   // Exception → invalid
            true
        } catch(_: Exception) { false }
    }

    /** 성별 코드(1,2,3,4,9) & 길이 1 */
    val isSexValid : LiveData<Boolean> =
        sexDigit.map { it.length == 1 && it[0] in "12349" }

    /* ── ④ 모두 통과 여부 → 버튼 활성화 ──────────────────────────── */
    val canProceed: LiveData<Boolean> =
        listOf(isNameValid, isBirthValid, isSexValid)
            .combineLatest { list -> list.all { it } }

    /* ── ⑤ 실명 인증 네트워크 상태 ──────────────────────────────── */
    private val _verifyState = MutableLiveData<VerifyState>(VerifyState.Idle)
    val verifyState : LiveData<VerifyState> = _verifyState

    /** 실명 + 주민등록번호 7자리 검증 */
    fun verifyIdentity() = viewModelScope.launch {
        _verifyState.value = VerifyState.Loading
        try {
            val ok = FakeRepo.verifyRealName(
                name.value.orEmpty(),
                birth.value.orEmpty(),
                sexDigit.value.orEmpty()
            )
            _verifyState.value =
                if (ok) VerifyState.Success
                else VerifyState.Error("이미 가입된 사용자입니다.")
        } catch(_: Exception) {
            _verifyState.value = VerifyState.Error("서버 통신 오류가 발생했습니다.")
        }
    }
    fun resetVerifyState() { _verifyState.value = VerifyState.Idle }

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z\\s]{1,20}$")
    }
}

/* ---------------- 테스트용 Mock ---------------- */
object FakeRepo {
    /** 이미 가입된 경우 -> false, 가입 이력이 없으면 true  */
    suspend fun verifyRealName(n: String, b: String, s: String): Boolean {
        delay(800)

        // 이미 가입된 사용자 조건
        val isDuplicated = (n == "sss" && b == "111111" && s == "1")

        // 중복이면 false, 신규이면 true
        return !isDuplicated
    }
}

/* ---------- LiveData 확장 : 여러 값을 combine ---------- */
fun <T> List<LiveData<T>>.combineLatest(block:(List<T>)->T) : LiveData<T> =
    MediatorLiveData<T>().also { m ->
        val data = MutableList(size){null as T?}
        forEachIndexed { i, src ->
            m.addSource(src){ v -> data[i]=v; if(data.all{ it!=null }) m.value=block(data.filterNotNull())}
        }
    }