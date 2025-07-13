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
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.DetailPage.ReviewDetailActivity
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.ExplorePage.ExploreFragment
import com.example.soar.MainActivity
import com.example.soar.R
import com.example.soar.databinding.FragmentHomeBinding
import com.google.api.Context

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
            SwipeCardItem(getString(R.string.home_swipe), R.drawable.swipe_img1, "https://www.naver.com/"),
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

        binding.category1.setOnClickListener{
            openExplore(1)
        }
        binding.category2.setOnClickListener{
            openExplore(2)
        }
        binding.category3.setOnClickListener{
            openExplore(3)
        }
        binding.category4.setOnClickListener{
            openExplore(4)
        }

        binding.ad1.setOnClickListener{
            Log.d("CLICK", "ad1 clicked")
            openWebPage("https://www.k-startup.go.kr/")
        }
        binding.ad2.setOnClickListener{
            openWebPage("https://www.worldjob.or.kr/")
        }
        binding.ad3.setOnClickListener{
            openWebPage("https://www.2030db.go.kr/")
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
