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
import com.example.soar.DetailPage.DetailPageActivity
import java.time.format.DateTimeFormatter
import android.util.TypedValue
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import com.facebook.shimmer.ShimmerFrameLayout
import androidx.core.view.isVisible
import com.example.soar.CalendarPage.CalendarFragment
import com.example.soar.MyPage.ProfileActivity
import com.example.soar.MyPage.RecordActivity
import com.example.soar.util.TouchBlockingToast
import com.example.soar.util.showBlockingToast


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



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var personalCardAdapter: PersonalCardAdapter // 타입 변경
    private lateinit var popularPolicyAdapter: PopularPolicyAdapter
    private lateinit var shimmer: ShimmerFrameLayout

    private val jankRunnables = mutableListOf<Runnable>()


    // 각 요청의 최초 응답 여부
    private var loadedLatest = false
    private var loadedPopular = false
    private var loadedAge = false

    private var firstShowAt = 0L
    private val minSkeletonMs = 190L   //
    private var isSkeletonHidden = false
    private var hideRunnable: Runnable? = null

    // 리빌 애니메이션 파라미터(영상 느낌값)
    private val revealDurationMs = 320L          // 컨텐츠 페이드/슬라이드
    private val shimmerFadeOutMs = 220L          // 오버레이 페이드아웃
    private val contentDimAlpha = 0.08f          // 로딩 중 컨텐츠 디밍 정도
    private val revealTranslateDp = 12f          // 위로 살짝 올라오는 거리
    private val overshoot = OvershootInterpolator(1.05f) // 카드 반동감


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shimmer = requireView().findViewById(R.id.home_skeleton)

        // 1) 구간별 ShimmerFrameLayout 참조
        val skelHeader = requireView().findViewById<ShimmerFrameLayout>(R.id.skel_header)
        val skelPager  = requireView().findViewById<ShimmerFrameLayout>(R.id.skel_viewpager)
        val skelMain   = requireView().findViewById<ShimmerFrameLayout>(R.id.skel_main)

        // 2) 빌더로 "각기 다른" 쉬머 만들기 (속도/방향/강도 다르게)
        fun shimmerHeader() = com.facebook.shimmer.Shimmer.AlphaHighlightBuilder()
            .setDuration(780)                     // 짧고 빠르게
            .setBaseAlpha(1.0f)
            .setHighlightAlpha(0.70f)
            .setIntensity(0.45f)                  // 하이라이트 폭 얇게
            .setDropoff(0.60f)                    // 경사 급하게
            .setDirection(com.facebook.shimmer.Shimmer.Direction.TOP_TO_BOTTOM)
            .setTilt(18f)
            .build()

        fun shimmerPager() = com.facebook.shimmer.Shimmer.AlphaHighlightBuilder()
            .setDuration(1650)                    // 중간 속도
            .setBaseAlpha(1.0f)
            .setHighlightAlpha(0.65f)
            .setIntensity(0.38f)
            .setDropoff(0.55f)
            .setDirection(com.facebook.shimmer.Shimmer.Direction.BOTTOM_TO_TOP)
            .setTilt(8f)
            .build()

        fun shimmerMainFast() = com.facebook.shimmer.Shimmer.AlphaHighlightBuilder()
            .setDuration(520)                      // 매우 빠르게 스쳐감
            .setBaseAlpha(1.0f)
            .setHighlightAlpha(0.80f)              // 번쩍임 강하게
            .setIntensity(0.55f)
            .setDropoff(0.40f)
            .setDirection(com.facebook.shimmer.Shimmer.Direction.LEFT_TO_RIGHT)
            .setTilt(22f)
            .build()

        fun shimmerMainSlow() = com.facebook.shimmer.Shimmer.AlphaHighlightBuilder()
            .setDuration(2300)                     // 느긋하게
            .setBaseAlpha(1.0f)
            .setHighlightAlpha(0.55f)
            .setIntensity(0.30f)
            .setDropoff(0.70f)
            .setDirection(com.facebook.shimmer.Shimmer.Direction.RIGHT_TO_LEFT)
            .setTilt(2f)
            .build()

// 3) 초기에 서로 "엇박"으로 시작 (비동기 느낌)
        skelHeader.setShimmer(shimmerHeader())
        skelHeader.startShimmer()

        skelPager.postDelayed({                    // 약간 늦게 시작
            skelPager.setShimmer(shimmerPager())
            skelPager.startShimmer()
        }, 220L)

        skelMain.postDelayed({
            // 메인은 빠른 모드로 먼저 시작
            skelMain.setShimmer(shimmerMainFast())
            skelMain.startShimmer()
        }, 680L)

// 4) "끊김/불균형" 연출: 주기적으로 멈췄다 다른 속도로 재시작
//    - 메인은 빠름/느림을 번갈아가며, 무작위 지연으로 재시작
        val jankRunnables = mutableListOf<Runnable>()

        fun scheduleJank(frame: ShimmerFrameLayout, modes: List<() -> com.facebook.shimmer.Shimmer>, baseDelay: LongRange) {
            val r = object : Runnable {
                override fun run() {
                    // 4-1) 잠깐 멈춤
                    frame.stopShimmer()

                    // 4-2) 무작위로 모드 선택 (속도·방향 바뀌는 느낌)
                    val mode = modes.random()
                    frame.setShimmer(mode())

                    // 4-3) 무작위 지연 뒤 다시 시작 (끊겨 보이게)
                    val delay = baseDelay.random()
                    frame.postDelayed({
                        frame.startShimmer()
                        // 다음 사이클 예약
                        frame.postDelayed(this, baseDelay.random())
                    }, delay)
                }
            }
            jankRunnables += r
            frame.postDelayed(r, baseDelay.random()) // 첫 사이클 예약
        }

// Header는 느닷없이 잠깐 멈췄다 동일 모드로 재개(짧은 지연, 더 자주 끊김)
        scheduleJank(
            skelHeader,
            modes = listOf(::shimmerHeader),
            baseDelay = 700L..1400L
        )

// Pager는 현재 모드만 유지하되, 재시작 텀을 길고 랜덤하게
        scheduleJank(
            skelPager,
            modes = listOf(::shimmerPager),
            baseDelay = 1500L..3000L
        )

// Main은 빠른/느린 모드를 랜덤으로 스왑 (체감 언밸런스의 핵심)
        scheduleJank(
            skelMain,
            modes = listOf(::shimmerMainFast, ::shimmerMainSlow),
            baseDelay = 900L..2600L
        )


        // 로딩 시작
        showSkeleton()


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


            binding.btnProfile.setOnClickListener {
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent)
            }

            binding.btnRecentlyViewed.setOnClickListener {
                val intent = Intent(requireContext(), RecordActivity::class.java)
                startActivity(intent)
            }

            binding.btnClosingSoon.setOnClickListener {
                openCalender()
            }

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

        setupPersonalCardRecyclerView()
        setupPopularBizRecyclerView() // RecyclerView 설정 함수 호출
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

    private fun openCalender() {

        val calendarFragment = CalendarFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, calendarFragment)
            .commit()
        (activity as? MainActivity)?.updateNavByTag("calender")
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setupPopularBizRecyclerView() {
        popularPolicyAdapter = PopularPolicyAdapter()
        binding.popularBizList.apply {
            adapter = popularPolicyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /** [추가] ViewModel의 LiveData를 관찰하는 함수 */
    private fun observeViewModel() {

        // 사용자 정보가 성공적으로 업데이트되었을 때
        homeViewModel.userInfo.observe(viewLifecycleOwner) { newInfo ->
            // 1. 받아온 최신 정보를 로컬(TokenManager)에 저장
            TokenManager.saveUserInfo(newInfo)

            // 2. UI 업데이트
            binding.tvUserNameGreeting.text = newInfo.userName

        }

        homeViewModel.latestPolicy.observe(viewLifecycleOwner) { latestPolicy ->
            if (!loadedLatest) {
                loadedLatest = true
                checkAndHideSkeleton()
            }

            if (latestPolicy != null) {
                // Case 1: 마감 임박 정책이 있는 경우
                binding.latestPolicyContent.visibility = View.VISIBLE
                binding.noLatestPolicyText.visibility = View.GONE
                binding.latestPolicyTitle.text = latestPolicy.policyName

                // dateLabel 파싱 (예: "사업 마감 D-8" -> "D-8")
                val dDay = latestPolicy.dateLabel.substringAfter("D-").let { "D-$it" }
                binding.latestPolicyDday.text = dDay

                // businessPeriodEnd 포맷팅 (예: "20250906" -> "2025.09.06")
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                    val date = LocalDate.parse(latestPolicy.businessPeriodEnd, formatter)
                    binding.latestPolicyDate.text =
                        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                } catch (e: Exception) {
                    binding.latestPolicyDate.text = latestPolicy.businessPeriodEnd
                }

                // 전체 섹션 클릭 시 상세 페이지로 이동
                binding.latestPolicyDday.setOnClickListener {
                    val intent = Intent(requireContext(), DetailPageActivity::class.java).apply {
                        putExtra("policyId", latestPolicy.policyId)
                    }
                    startActivity(intent)
                }

            } else {
                // Case 2: 마감 임박 정책이 없는 경우
                binding.latestPolicyContent.visibility = View.GONE
                binding.noLatestPolicyText.visibility = View.VISIBLE
                binding.sectionLogin.setOnClickListener(null) // 클릭 이벤트 제거
            }
        }
        homeViewModel.popularPolicies.observe(viewLifecycleOwner) { policies ->

            if (!loadedPopular) {
                loadedPopular = true
                checkAndHideSkeleton()
            }

            // 조건: API로 받은 데이터가 2개 초과일 때만 섹션을 보여줌
            if (policies.size > 2) {
                binding.section5.visibility = View.VISIBLE
                popularPolicyAdapter.submitList(policies)
            } else {
                binding.section5.visibility = View.GONE
            }
        }

        homeViewModel.agePopularPolicies.observe(viewLifecycleOwner) { policies ->

            if (!loadedAge) {
                loadedAge = true
                checkAndHideSkeleton()
            }

            if (policies.size > 2) {
                binding.sectionPopular.visibility = View.VISIBLE
                // 제목의 'OO대' 부분 업데이트
                binding.textAgeGroup.text = policies.first().ageGroup
                personalCardAdapter.submitList(policies)
            } else {
                binding.sectionPopular.visibility = View.GONE
            }
        }

        homeViewModel.shouldForceLogout.observe(viewLifecycleOwner) { must ->
            if (must == true) {
                // 1) 토큰 정리 (프로젝트 메서드에 맞게 변경)
                TokenManager.clearTokens()

                // 2) 토스트 + 로그인 화면 이동
                requireActivity().showBlockingToast(
                    message = "세션이 만료되었어요. 다시 로그인해 주세요.",
                    long = false,
                    hideCancel = true
                )
                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }
        }

        // 에러 발생 시
        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
        }
    }

    /** [추가] 로그인 상태에 따라 UI를 설정하는 함수 */
    private fun setupLoginStateUI() {
        val accessToken = TokenManager.getAccessToken()

        showSkeleton()

        loadedLatest = false; loadedPopular = false; loadedAge = false

        if (!accessToken.isNullOrEmpty()) {
            // --- 로그인 상태일 때 ---
            binding.section1.visibility = View.GONE
            binding.sectionLogin.visibility = View.VISIBLE
            binding.sectionPopular.visibility = View.VISIBLE

            // 1. 우선 로컬에 저장된 정보로 UI를 즉시 업데이트
            val localUserInfo = TokenManager.getUserInfo()
            val userName =
                localUserInfo?.userName ?: TokenManager.getSignInInfo()?.userName ?: "사용자"
            binding.tvUserNameGreeting.text = userName

            // 2. 서버에 최신 사용자 정보 요청 (네트워크 상태에 따라 시간이 걸릴 수 있음)
            homeViewModel.fetchUserInfoWithRetry(
                maxAttempts = 3,
                initialBackoffMs = 600L
            )
            homeViewModel.fetchLatestPolicy() // ✨ 마감 임박 정책 API 호출
            homeViewModel.fetchAgePopularPolicies()

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
            loadedLatest = true
            loadedAge = true
        }

        homeViewModel.fetchPopularPolicy()
    }

    // personal_card RecyclerView 설정 함수
    private fun setupPersonalCardRecyclerView() {
        personalCardAdapter = PersonalCardAdapter { policy ->
            homeViewModel.toggleBookmarkForAgePolicy(policy)
        }
        binding.personalCard.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = personalCardAdapter
        }
    }

    private fun showSkeleton() {
        isSkeletonHidden = false
        firstShowAt = System.currentTimeMillis()

        shimmer.isVisible = true
        shimmer.alpha = 1f
        shimmer.startShimmer() // 이 한 줄이 모든 자식 Shimmer들을 활성화합니다.

        // ✅ 실제 콘텐츠는 완전 숨김 (비침/터치 방지)
        val content = binding.root.findViewById<View>(R.id.content_root)
        content.clearAnimation()
        content.visibility = View.INVISIBLE
        content.alpha = 0f
        content.translationY = 0f

        // 컨텐츠는 완전 투명 금지(배경 누수 방지)
        binding.root.findViewById<View>(R.id.content_root).apply {
            animate().cancel()
            animate().alpha(contentDimAlpha).setDuration(120).start()
        }
    }


    private fun hideSkeleton() {
        if (isSkeletonHidden) return

        val elapsed = System.currentTimeMillis() - firstShowAt
        val delay = (minSkeletonMs - elapsed).coerceAtLeast(0)

        hideRunnable?.let { shimmer.removeCallbacks(it) }
        hideRunnable = Runnable {
            if (isSkeletonHidden || _binding == null) return@Runnable
            isSkeletonHidden = true

            val content = binding.root.findViewById<View>(R.id.content_root)

            // 스켈레톤 페이드아웃
            shimmer.stopShimmer()
            shimmer.animate()
                .alpha(0f)
                .setDuration(shimmerFadeOutMs)
                .withEndAction {
                    shimmer.isVisible = false
                    shimmer.alpha = 1f
                }
                .start()

            // ✅ 실제 콘텐츠 보이기 + 리빌 애니메이션
            content.visibility = View.VISIBLE
            content.alpha = 0f
            content.translationY = dp(revealTranslateDp)
            content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(revealDurationMs)
                .setInterpolator(
                    AnimationUtils.loadInterpolator(
                        requireContext(),
                        android.R.interpolator.fast_out_slow_in
                    )
                )
                .withEndAction { startStaggerAnimations() }
                .start()
        }
        shimmer.postDelayed(hideRunnable!!, delay)
    }

    private fun checkAndHideSkeleton() {
        // 필요한 것만 충족하면 감춤.
        // 로그인 상태별로 조건을 달리해도 됩니다.
        if (loadedLatest && loadedPopular) {
            hideSkeleton()
        }
        if(!loadedAge){
            binding.sectionPopularTitle.visibility = View.GONE
        }
    }

    private fun startStaggerAnimations() {
        // RecyclerView/섹션에 레이아웃 애니메이션 부여
        val la = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_anim_stagger_70)

        binding.personalCard.layoutAnimation = la
        binding.personalCard.scheduleLayoutAnimation()

        binding.popularBizList.layoutAnimation = la
        binding.popularBizList.scheduleLayoutAnimation()

        // 광고 섹션(RecyclerView)이면 동일하게, LinearLayout이라면 직접 뷰 애니메이션을 돌립니다.
        binding.section4.layoutAnimation = la
        binding.section4.scheduleLayoutAnimation()

        // 큰 섹션 박스들에 ‘살짝’ 반동감(옵션)
        listOf(
            binding.sectionLogin,
            binding.section3,
            binding.sectionPopular,
            binding.section5,
            binding.section6
        ).forEach { v ->
            v.scaleX = 0.98f
            v.scaleY = 0.98f
            v.animate().scaleX(1f).scaleY(1f)
                .setDuration(220L)
                .setInterpolator(overshoot)
                .start()
        }
    }

    private fun dp(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)





    override fun onStop() {
        super.onStop()
        // ✅ 화면 떠날 때 혹시 남아있을 차단뷰/토스트 정리 (안전장치)
        TouchBlockingToast.clear(requireActivity())
    }


    override fun onDestroyView() {
        // 예약된 hide 콜백 제거(메모리/크래시 방지)
        // ✨ 애니메이션 정리 코드 추가
        hideRunnable?.let { shimmer.removeCallbacks(it) }
        hideRunnable = null
        _binding = null

        jankRunnables.forEach { runnable ->
            shimmer.removeCallbacks(runnable)
            // skelHeader/skelPager/skelMain 각각에서도 removeCallbacks 해주는게 안전
            view?.findViewById<ShimmerFrameLayout>(R.id.skel_header)?.removeCallbacks(runnable)
            view?.findViewById<ShimmerFrameLayout>(R.id.skel_viewpager)?.removeCallbacks(runnable)
            view?.findViewById<ShimmerFrameLayout>(R.id.skel_main)?.removeCallbacks(runnable)
        }
        jankRunnables.clear()

        super.onDestroyView()
    }

}
