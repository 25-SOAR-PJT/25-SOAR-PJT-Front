package com.example.soar.HomePage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.EntryPage.Onboarding.OnBoardingActivity
import com.example.soar.EntryPage.SignIn.LoginActivity
import com.example.soar.R
import com.example.soar.databinding.FragmentHomeBinding

data class SwipeCardItem(
    val title: String,
    val imageResId: Int,
    val url: String
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
            binding.sectionFirst.visibility = View.GONE
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

        val adapter = SwipeCardAdapter(cardList)
        binding.sectionSecond.adapter = adapter
        binding.sectionSecond.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.dotsIndicator.attachTo(binding.sectionSecond)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
