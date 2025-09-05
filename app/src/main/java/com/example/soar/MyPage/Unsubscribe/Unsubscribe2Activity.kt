// Unsubscribe2Activity.kt
package com.example.soar.MyPage.Unsubscribe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.soar.R
import com.example.soar.databinding.ActivityUnsubscribe2Binding
import com.example.soar.Network.user.AuthRepository
import com.example.soar.Network.TokenManager
import kotlinx.coroutines.launch
import androidx.core.widget.doOnTextChanged

class Unsubscribe2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribe2Binding
    private val repo by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribe2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.text_title).text = getString(R.string.unsubscribe)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 비밀번호 입력 시 버튼 활성/비활성
        binding.pw1.doOnTextChanged { text, _, _, _ ->
            binding.btnUnsubscribe.isEnabled = !text.isNullOrBlank()
            binding.pw1Error.isVisible = false
        }

        binding.btnUnsubscribe.setOnClickListener {
            val pw = binding.pw1.text?.toString()?.trim().orEmpty()
            if (pw.isEmpty()) {
                showError("비밀번호를 입력해주세요.")
                return@setOnClickListener
            }

            binding.btnUnsubscribe.isEnabled = false

            lifecycleScope.launch {
                repo.deleteUser(pw).onSuccess {
                    // 1) 로컬 자원 정리
                    TokenManager.clearTokens()

                    // 2) 성공 화면으로 이동 + 전체 스택 제거
                    val intent = Intent(this@Unsubscribe2Activity, UnsubscribeSucessActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    // 현재 액티비티는 자동 종료됨(스택 클리어)
                }.onFailure { e ->
                    binding.btnUnsubscribe.isEnabled = true
                    showError(e.message ?: "회원 탈퇴에 실패했습니다.")
                }
            }
        }
    }

    private fun showError(msg: String) {
        // 하단 에러 레이아웃 노출 + 메시지 반영
        binding.pw1Error.isVisible = true
        binding.pw1ErrorText.text = msg
    }
}
