package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.soar.MainActivity
import com.example.soar.Network.TokenManager
import com.example.soar.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accessToken = TokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            // ✨ 1. 로그인 정보 가져오기
            val signInInfo = TokenManager.getSignInInfo()
            val userName = signInInfo?.userName ?: "사용자" // 이름이 없을 경우 기본값

            // ✨ 2. 인사말 TextView 설정 및 표시
            b.tvGreeting.text = "${userName}님, 안녕하세요!"
            b.tvGreeting.visibility = View.VISIBLE

            // 로그아웃 버튼 표시 및 리스너 설정
            b.btnLogout.visibility = View.VISIBLE
            b.btnLogout.setOnClickListener {
                TokenManager.clearTokens()

                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
            // 토큰이 없으면 모든 관련 UI 숨김
            b.tvGreeting.visibility = View.GONE
            b.btnLogout.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}