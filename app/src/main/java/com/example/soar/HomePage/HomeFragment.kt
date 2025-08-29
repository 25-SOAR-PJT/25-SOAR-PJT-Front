package com.example.soar.HomePage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.soar.AlarmPage.AlarmActivity
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.ExplorePage.ExploreFragment
import com.example.soar.MainActivity
import com.example.soar.Network.TokenManager
import com.example.soar.R
import com.example.soar.databinding.FragmentHomeBinding
import java.time.LocalDate
import androidx.fragment.app.viewModels // 추가
import android.widget.Toast // 추가

data class SwipeCardItem(
    val title: String,
    val imageResId: Int,
    val url: String
)

data class adItem(
    val label: String,
    val tile: String,
    val category: String,
    val keyword: String
)


data class ProgramCard(
    val location: String,
    val title: String,
    val date: LocalDate,
    var isBookmarked: Boolean = false
)

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseFirst.setOnClickListener {
            binding.section1.visibility = View.GONE
        }
        binding.btnToLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        val cardList = listOf(
            SwipeCardItem(
                getString(R.string.home_swipe),
                R.drawable.swipe_img1,
                "https://www.naver.com/"
            ),
            SwipeCardItem("이건 제목 2", R.drawable.swipe_img2, "https://example.com/2"),
            SwipeCardItem("이건 제목 3", R.drawable.swipe_img3, "https://example.com/3"),
            SwipeCardItem("이건 제목 4", R.drawable.swipe_img4, "https://example.com/4")
        )
        val adList = listOf(
            adItem("당신의 시작을 응원합니다", "취업 준비에 필요한\\n정보를 한눈에 확인해보세요", "일자리", "취·창업 컨설팅"),
            adItem("신혼부부를 위한 지원", "새로운 보금자리 마련을\\n지금 바로 시작해보세요", "주거", "신혼부부 주거지원"),
            adItem("미래를 위한 배움", "안전하게 배우고\\n성장할 수 있는 교육 과정", "교육", "안전 교육"),
            adItem("첫 면접, 어렵지 않아요", "면접 준비를 위한\\n실전 꿀팁을 만나보세요", "일자리", "면접 지원"),
            adItem("내 삶을 바꾸는 공부", "IT·마케팅 실무 교육으로\\n새로운 기회를 만들어보세요", "교육", "IT·마케팅 교육")
        )
        val personalCardList = listOf(
            ProgramCard("서울특별시 노원구", "청년취업해Dream 사업", LocalDate.of(2025, 9, 4)),
            ProgramCard("서울특별시 강남구", "청년 어학자격 응시료 지원", LocalDate.of(2025, 7, 21)),
            ProgramCard("서울특별시 성동구", "청년 창업 프로젝트", LocalDate.of(2025, 8, 2))
        )

        val personalCardAdapter = PersonalCardAdapter(personalCardList)
        binding.personalCard.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = personalCardAdapter
        }

        val swipeAdapter = SwipeCardAdapter(cardList)
        binding.section2.adapter = swipeAdapter
        binding.section2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.dotsIndicator.attachTo(binding.section2)

        val randomItem = adList.random()
        val randomList = listOf(randomItem)
        val adapter = HomeAdAdapter(randomList)
        binding.section4.layoutManager = LinearLayoutManager(requireContext())
        binding.section4.adapter = adapter

        binding.category1.setOnClickListener {
            openExplore(0)
        }
        binding.category2.setOnClickListener {
            openExplore(1)
        }
        binding.category3.setOnClickListener {
            openExplore(2)
        }
        binding.category4.setOnClickListener {
            openExplore(3)
        }

        binding.ad1.setOnClickListener {
            Log.d("CLICK", "ad1 clicked")
            openWebPage("https://www.k-startup.go.kr/")
        }
        binding.ad2.setOnClickListener {
            openWebPage("https://www.worldjob.or.kr/")
        }
        binding.ad3.setOnClickListener {
            openWebPage("https://www.2030db.go.kr/")
        }


        // 임시
        binding.btnAlarm.setOnClickListener {
            val intent = Intent(requireContext(), AlarmActivity::class.java)
            startActivity(intent)
        }


        // 1. 로그인 상태 확인 및 UI 분기 처리
        val accessToken = TokenManager.getAccessToken()

//        // 알람 버튼 클릭 로그인 분기
//        binding.btnAlarm.setOnClickListener {
//            if (!accessToken.isNullOrEmpty()) {
//                // 로그인 상태일 때: 알림 페이지로 이동
//                val intent = Intent(requireContext(), AlarmActivity::class.java)
//                startActivity(intent)
//            } else {
//                // 비로그인 상태일 때: 로그인 페이지로 이동
//                val intent = Intent(requireContext(), LoginActivity::class.java)
//                startActivity(intent)
//            }
//        }

        if (!accessToken.isNullOrEmpty()) {
            // --- 로그인 상태일 때 ---
            binding.section1.visibility = View.GONE
            binding.sectionLogin.visibility = View.VISIBLE
            binding.sectionPopular.visibility = View.VISIBLE

            // 사용자 이름 가져와서 인사말 설정
            val signInInfo = TokenManager.getSignInInfo()
            val userName = signInInfo?.userName ?: "사용자"
            binding.tvUserNameGreeting.text = userName

        } else {
            // --- 비로그인 상태일 때 ---
            binding.section1.visibility = View.VISIBLE
            binding.sectionLogin.visibility = View.GONE
            binding.sectionPopular.visibility = View.GONE

            // 비로그인 섹션의 버튼 리스너 설정
            binding.btnCloseFirst.setOnClickListener {
                binding.section1.visibility = View.GONE
            }
            binding.btnToLogin.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }

        setupLoginStateUI()
        observeViewModel()
    }

    private fun openExplore(id: Int) {
        val bundle = Bundle().apply {
            putInt("category_id", id)
        }

        val exploreFragment = ExploreFragment().apply {
            arguments = bundle
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, exploreFragment)
            .commit()
        (activity as? MainActivity)?.updateNavByTag("explore")
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    /** [추가] ViewModel의 LiveData를 관찰하는 함수 */
    private fun observeViewModel() {
        // 사용자 정보가 성공적으로 업데이트되었을 때
        homeViewModel.userInfo.observe(viewLifecycleOwner) { newInfo ->
            // 1. 받아온 최신 정보를 로컬(TokenManager)에 저장
            TokenManager.saveUserInfo(newInfo)

            // 2. UI 업데이트
            binding.tvUserNameGreeting.text = newInfo.userName
            Toast.makeText(requireContext(), "최신 사용자 정보를 업데이트했습니다.", Toast.LENGTH_SHORT).show()
        }

        // 에러 발생 시
        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), "정보 업데이트 실패: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    /** [추가] 로그인 상태에 따라 UI를 설정하는 함수 */
    private fun setupLoginStateUI() {
        val accessToken = TokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            // --- 로그인 상태일 때 ---
            binding.section1.visibility = View.GONE
            binding.sectionLogin.visibility = View.VISIBLE
            binding.sectionPopular.visibility = View.VISIBLE

            // 1. 우선 로컬에 저장된 정보로 UI를 즉시 업데이트
            val localUserInfo = TokenManager.getUserInfo()
            val userName = localUserInfo?.userName ?: TokenManager.getSignInInfo()?.userName ?: "사용자"
            binding.tvUserNameGreeting.text = userName

            // 2. 서버에 최신 사용자 정보 요청 (네트워크 상태에 따라 시간이 걸릴 수 있음)
            homeViewModel.fetchUserInfo()

        } else {
            // --- 비로그인 상태일 때 ---
            binding.section1.visibility = View.VISIBLE
            binding.sectionLogin.visibility = View.GONE
            binding.sectionPopular.visibility = View.GONE

            // 비로그인 섹션의 버튼 리스너 설정
            binding.btnCloseFirst.setOnClickListener {
                binding.section1.visibility = View.GONE
            }
            binding.btnToLogin.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
