package com.example.soar.MyPage

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.soar.R
import com.example.soar.Utill.ErrorMessageHelper
import com.example.soar.Utill.FocusErrorController
import com.example.soar.Utill.FocusErrorControllerGroup
import com.example.soar.Utill.combineLatest
import com.example.soar.databinding.ActivityChangePwBinding
import com.example.soar.databinding.ActivityFind2Binding
import com.google.android.material.textfield.TextInputLayout
import kotlin.getValue

class FindActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFind2Binding

    private val vm: FindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFind2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tilName.setErrorTextAppearance(R.style.Font_Caption_Regular)
        binding.tilResidentBirth.setErrorTextAppearance(R.style.Font_Caption_Regular)

        binding.etResidentBirth.letterSpacing = 0f

        /* ── 입력 → VM ─────────────────────────────────── */
        binding.etName.doAfterTextChanged           { vm.name.value      = it.toString() }
        binding.etResidentBirth.doAfterTextChanged { editable ->
            // ① 글자가 있으면 간격을 주고, 없으면 0
            binding.etResidentBirth.letterSpacing =
                if (editable.isNullOrEmpty()) 0f else 0.7f   // 0.3~0.4 사이에서 취향대로 조절

            // ② VM 값 반영
            vm.birth.value = editable.toString()

            // ③ 6자리 다 입력되면 성별칸으로 포커스 이동
            if (editable?.length == 6) binding.etResidentSex.requestFocus()
        }
        binding.etResidentSex.doAfterTextChanged    { vm.sexDigit.value  = it.toString() }

        /* ── touched flag ─────────────────────────────── */
        setTouchFlag(binding.tilName,          vm.nameTouched )
        setTouchFlag(binding.tilResidentBirth, vm.birthTouched)
        setTouchFlag(binding.tilResidentSex,   vm.sexTouched )

        /* ── maxLength ───────────────────────────────── */
        binding.etResidentBirth.filters = arrayOf(InputFilter.LengthFilter(6))
        binding.etResidentSex.filters   = arrayOf(InputFilter.LengthFilter(1))



        /* ── IME DONE → 다음 ─────────────────────────── */
        binding.etResidentSex.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE && vm.canProceed.value == true) {
                binding.btnFindId.performClick(); true
            } else false
        }



        /* ── 에러 헬퍼 ────────────────────────────────── */
        val helper = ErrorMessageHelper(this,
            R.drawable.ic_error, -3, R.color.ref_red_500)

        fun showErr(til: TextInputLayout, msg: String) {
            helper.showError(til, false, true, msg)
        }
        /* 이름만 TIL-error 사용 */
        fun showNameErr(msg:String) = helper.showError(binding.tilName, false, true, msg)

        /* 주민번호 그룹 전용 TextView */
        fun showResidentErr(msg:String){
            binding.tvResidentErr.text = msg
            binding.tvResidentErr.visibility = View.VISIBLE
        }
        fun clearResidentErr(){ binding.tvResidentErr.visibility = View.GONE }

        /* ── FocusErrorController ────────────────────── */
        FocusErrorController(
            binding.etName, binding.tilName,
            isValid         = { vm.isNameValid.value == true },
            getErrorMessage = { getString(R.string.error_name_format) },
            showError       = { msg -> showErr(binding.tilName, msg) }
        )

        /* ── 주민번호 그룹 포커스 색상 ─────────────────── */
        FocusErrorControllerGroup(
            edits  = listOf(binding.etResidentBirth, binding.etResidentSex),
            tils   = listOf(binding.tilResidentBirth, binding.tilResidentSex),
            hyphen = binding.ivHyphen,
            defaultColor = this.getColor(R.color.textinput_stroke),
            focusedColor = this.getColor(R.color.ref_blue_700),
            errorColor   = this.getColor(R.color.ref_red_500),

            /* 검증 로직 */
            isValid = { vm.isBirthValid.value == true && vm.isSexValid.value == true },

            /* 메시지 선택 */
            getErrorMessage = {
                if (vm.isBirthValid.value != true)
                    getString(R.string.error_birth_format)
                else
                    getString(R.string.error_sex_digit)
            },

            showError  = ::showResidentErr,
            clearError = ::clearResidentErr
        )

        /* ── ViewModel 상태 변경 감지 → UI 업데이트 ──────────────── */
        // LiveData를 관찰하여 유효성 상태가 변경될 때마다 실시간으로 에러 메시지를 제어합니다.

        // 이름 유효성 관찰
        vm.isNameValid.observe(this) { isValid ->
            // 사용자가 터치한 적이 있고(touched), 현재 값이 유효하지 않을 때만 에러를 표시합니다.
            if (vm.nameTouched.value == true && !isValid) {
                showNameErr(getString(R.string.error_name_format))
            } else {
                // 유효하다면 에러를 지웁니다.
                binding.tilName.error = null
            }
        }

        // 주민번호 그룹 유효성 관찰 (생년월일, 성별)
        listOf(vm.isBirthValid, vm.isSexValid).combineLatest { (isBirthOk, isSexOk) ->
            val isGroupValid = isBirthOk && isSexOk
            val isGroupTouched = vm.birthTouched.value == true || vm.sexTouched.value == true

            if (isGroupTouched && !isGroupValid) {
                // 에러 상태일 때: FocusErrorControllerGroup이 포커스 아웃 시 처리하므로 여기서는 생략 가능
                // 단, 실시간으로 에러를 보여주고 싶다면 아래 주석을 해제하세요.
                /*
                val errorMsg = if (isBirthOk != true) getString(R.string.error_birth_format)
                               else getString(R.string.error_sex_digit)
                showResidentErr(errorMsg)
                */
            } else {
                // 유효한 상태가 되면 즉시 에러 메시지를 숨깁니다.
                clearResidentErr()
            }
        }.observe(this) { /* observe를 시작하기 위해 비워둠 */ }



        // TODO: 아이디(이메일) / 이름 / 주민 번호  부분 에러 핸들링 부탁드림 (회원 가입이랑 동일)
        // TODO : 제대로 다 입력 받으면 버튼 활성화 + FindResultActivity로 넘어가기
        // TODO : 비번 찾기는 버튼 클릭 시 토스트 메세지 띄우기.

        // 앱바
        findViewById<TextView>(R.id.text_title).text = ""
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 아이디/ 비번 찾기 부분 파란선 이동 로직
        binding.root.post {
            val pl = binding.tabBar.paddingLeft
            val pr = binding.tabBar.paddingRight
            val contentW = binding.tabBar.width - pl - pr
            val half = contentW / 2

            binding.line.layoutParams = binding.line.layoutParams.apply { width = half }
            binding.line.x = pl.toFloat()            // 왼쪽 절반의 시작점
            binding.line.requestLayout()

            selectTab(binding.toId, binding.toPw)
        }

        // 아이디 찾기, 비번 찾기 둘다 한 화면에 있고 visibility만 조정.
        binding.toId.setOnClickListener {
            selectTab(binding.toId, binding.toPw)
            binding.layoutFindID.visibility = View.VISIBLE
            binding.layoutFindPW.visibility = View.GONE
        }
        binding.toPw.setOnClickListener {
            selectTab(binding.toPw, binding.toId)
            binding.layoutFindID.visibility = View.GONE
            binding.layoutFindPW.visibility = View.VISIBLE
        }


    }


    private fun moveline(target: TextView) {
        val line = binding.line
        val container = binding.tabBar

        val pl = container.paddingLeft
        val pr = container.paddingRight
        val contentW = container.width - pl - pr
        val half = contentW / 2

        // 왼쪽 탭이면 pl, 오른쪽 탭이면 pl + half 위치로
        val targetStartX = (pl + if (target.id == R.id.to_id) 0 else half).toFloat()

        val widthAnim = ValueAnimator.ofInt(line.width, half).apply {
            duration = 220
            addUpdateListener {
                line.layoutParams = line.layoutParams.apply { width = it.animatedValue as Int }
                line.requestLayout()
            }
        }
        val xAnim = ObjectAnimator.ofFloat(line, View.X, line.x, targetStartX).apply { duration = 220 }

        AnimatorSet().apply { playTogether(widthAnim, xAnim); start() }
    }

    private fun selectTab(selected: TextView, other: TextView) {
        selected.setTextColor(ContextCompat.getColor(this, R.color.ref_blue_500))
        other.setTextColor(ContextCompat.getColor(this, R.color.semantic_text_strong))
        moveline(selected)
    }

    private fun setTouchFlag(til: TextInputLayout, flag: MutableLiveData<Boolean>) {
        til.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) flag.value = true
        }
    }

}