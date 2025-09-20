package com.example.soar.MyPage.Unsubscribe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.Network.TokenManager
import com.example.soar.databinding.ActivityUnsubscribeBinding
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.soar.util.showBlockingToast
import com.example.soar.Network.user.AuthRepository

class UnsubscribeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnsubscribeBinding
    private val repo by lazy { AuthRepository() }

    private fun isKakaoUser(): Boolean {
        // 저장된 플래그/로그인정보/유저정보 중 하나라도 카카오면 true
        val savedFlag = TokenManager.isKakaoUser()
        val byProvider = TokenManager.getSignInInfo()?.socialProvider?.equals("KAKAO", true) == true
        val byUserInfo = TokenManager.getUserInfo()?.isKakaoUser == true
        return savedFlag || byProvider || byUserInfo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnsubscribeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNo.setOnClickListener { finish() }

        binding.btnYes.setOnClickListener {
            if (isKakaoUser()) {
                reauthKakaoThenDelete()
            } else {
                startActivity(Intent(this, Unsubscribe2Activity::class.java))
            }
        }
    }
    private fun reauthKakaoThenDelete() {
        binding.btnYes.isEnabled = false

        // 1) 카카오 재로그인 (톡 가능 → 톡, 아니면 계정)
        val loginAction: () -> Unit = {
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null || token == null) {
                    binding.btnYes.isEnabled = true
                    showBlockingToast("카카오 로그인 실패: ${error?.message ?: "알 수 없는 오류"}", hideCancel = true)
                    return@loginWithKakaoAccount
                }
                // 2) 서버에 카카오 토큰으로 재로그인(앱 JWT 재발급)
                lifecycleScope.launch {
                    repo.kakaoLogin(token.accessToken).onSuccess {
                        // 3) 새 앱 토큰으로 카카오 탈퇴 API 호출
                        repo.deleteKakaoUser().onSuccess {
                            // 4) 로컬 정리 & 성공 화면
                            TokenManager.clearTokens()
                            val intent = Intent(this@UnsubscribeActivity, UnsubscribeSucessActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        }.onFailure { e ->
                            binding.btnYes.isEnabled = true
                            showBlockingToast(e.message ?: "카카오 회원 탈퇴 실패", hideCancel = true)
                        }
                    }.onFailure { e ->
                        binding.btnYes.isEnabled = true
                        showBlockingToast(e.message ?: "서버 재인증 실패", hideCancel = true)
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    // 톡 실패 시 계정 로그인으로 폴백
                    loginAction()
                } else if (token != null) {
                    lifecycleScope.launch {
                        repo.kakaoLogin(token.accessToken).onSuccess {
                            repo.deleteKakaoUser().onSuccess {
                                TokenManager.clearTokens()
                                val intent = Intent(this@UnsubscribeActivity, UnsubscribeSucessActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            }.onFailure { e ->
                                binding.btnYes.isEnabled = true
                                showBlockingToast(e.message ?: "카카오 회원 탈퇴 실패", hideCancel = true)
                            }
                        }.onFailure { e ->
                            binding.btnYes.isEnabled = true
                            showBlockingToast(e.message ?: "서버 재인증 실패", hideCancel = true)
                        }
                    }
                } else {
                    loginAction()
                }
            }
        } else {
            // 톡 미설치 → 계정 로그인
            loginAction()
        }
    }
}
