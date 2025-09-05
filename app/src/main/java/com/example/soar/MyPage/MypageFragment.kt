package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.MainActivity
import com.example.soar.Network.TokenManager
import com.example.soar.databinding.FragmentMypageBinding
import androidx.fragment.app.viewModels // viewModels import 추가
import com.example.soar.util.showBlockingToast
import com.example.soar.MyPage.Unsubscribe.UnsubscribeActivity

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MypageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ✨ 추가: 액티비티가 다시 시작될 때마다 데이터를 갱신합니다.
    override fun onResume() {
        super.onResume()
        val accessToken = TokenManager.getAccessToken()
        if (!accessToken.isNullOrEmpty()) {

            // 로그인 정보 가져오기
            val localUserInfo = TokenManager.getUserInfo()
            val userName = localUserInfo?.userName ?: TokenManager.getUserInfo()?.userName ?: "사용자"
            val userAddress = localUserInfo?.userAddress ?: "거주하시는 지역을 설정해주세요"

            // 유저 데이터 바인딩
            binding.textUserName.text = userName
            binding.textUserAddress.text = userAddress

            viewModel.fetchUserActivityCounts()

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containorProfile.setOnClickListener{
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.btnToApplied.setOnClickListener{
            val intent = Intent(requireContext(), AppliedActivity::class.java)
            startActivity(intent)
        }
        binding.btnToComment.setOnClickListener{
            val intent = Intent(requireContext(), CommentActivity::class.java)
            startActivity(intent)
        }
        binding.btnRecentlyViewed.setOnClickListener{
            val intent = Intent(requireContext(), RecordActivity::class.java)
            startActivity(intent)
        }
        binding.btnSoarGuide.setOnClickListener{
            val intent = Intent(requireContext(), OnBoardingActivity::class.java)
            startActivity(intent)
        }
        binding.btnAnnouncement.setOnClickListener{
            val intent = Intent(requireContext(), AnnouncementActivity::class.java)
            startActivity(intent)
        }
        binding.btnPolicy.setOnClickListener{
            val intent = Intent(requireContext(), PolicyActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            val accessToken = TokenManager.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                TokenManager.clearTokens()

                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }

            else {
                val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                startActivity(intent)
                requireActivity().finish()
            }
        }
        binding.btnUnsubscribe.setOnClickListener{
            val intent = Intent(requireContext(), UnsubscribeActivity::class.java)
            startActivity(intent)
        }

        val userViews = listOf(
            binding.containorProfile,
            binding.containerUserPreview,
            binding.containerUserActivity,
            binding.btnUnsubscribe
        )

        fun setLoggedInViewsVisibility(isVisible: Boolean) {
            val visibility = if (isVisible) View.VISIBLE else View.GONE
            userViews.forEach {
                it.visibility = visibility
            }
        }

        val accessToken = TokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            // 로그인 상태일 시 유저 관련 UI 보임
            setLoggedInViewsVisibility(true)

            // 로그인 정보 가져오기
            val localUserInfo = TokenManager.getUserInfo()
            val userName = localUserInfo?.userName ?: TokenManager.getUserInfo()?.userName ?: "사용자"
            val userAddress = localUserInfo?.userAddress ?: "거주하시는 지역을 설정해주세요"

            // 유저 데이터 바인딩
            binding.textUserName.text = userName
            binding.textUserAddress.text = userAddress
            binding.btnLoginText.text = "로그아웃"

            setupObservers()

        } else {
            // 토큰이 없으면 모든 관련 UI 숨김
            setLoggedInViewsVisibility(false)
            binding.btnLoginText.text = "로그인"
        }

    }

    private fun setupObservers() {
        viewModel.appliedPolicyCount.observe(viewLifecycleOwner) { count ->
            binding.appliedCount.text = count.toString()
        }

        viewModel.commentCount.observe(viewLifecycleOwner) { count ->
            binding.commentCount.text = count.toString()
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            showBlockingToast(errorMessage, hideCancel = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}