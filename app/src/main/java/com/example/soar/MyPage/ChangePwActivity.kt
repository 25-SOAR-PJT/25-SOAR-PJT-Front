package com.example.soar.MyPage

import android.os.Bundle
import android.provider.Settings.Global.getString
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityChangePwBinding

class ChangePwActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePwBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.text_title).text = getString(R.string.change_pw)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateUi() }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.pw1.addTextChangedListener(watcher)
        binding.pw2.addTextChangedListener(watcher)

        updateUi()
    }

    private fun isValidPassword(pw: String): Boolean {
        // 8~20자, 영문 소문자 + 숫자 조합
        return pw.matches(Regex("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,20}$"))
    }

    private fun updateUi() {
        val p1 = binding.pw1.text?.toString().orEmpty()
        val p2 = binding.pw2.text?.toString().orEmpty()
        val formatOk = isValidPassword(p1)
        val matchOk  = p1 == p2

        // 기본값 초기화
        fun clearErrors() {
            binding.pw1Error.visibility = View.GONE
            binding.pw2Error.visibility = View.GONE
            binding.pw1.setBackgroundResource(R.drawable.round_background4)
            binding.pw2.setBackgroundResource(R.drawable.round_background4)
        }

        clearErrors()

        when {
            p1.isNotBlank() && !formatOk -> {
                // 형식 에러 → pw1만 빨간 테두리 + 메시지
                binding.pw1.setBackgroundResource(R.drawable.round_background4_error)
                binding.pw1Error.visibility = View.VISIBLE
            }
            p2.isNotBlank() && !matchOk -> {
                // 불일치 에러 → pw2만 빨간 테두리 + 메시지
                binding.pw2.setBackgroundResource(R.drawable.round_background4_error)
                binding.pw2Error.visibility = View.VISIBLE
            }
        }

        binding.btnChange.isEnabled = formatOk && matchOk
    }
}
