package my_app.soar.EntryPage.Onboarding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import my_app.soar.MainActivity
import my_app.soar.R
import my_app.soar.databinding.ActivityOnboardingPageBinding  // 🔹 자동 생성된 바인딩 클래스


class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingPageBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnBoardAdapter

    private val pages by lazy {
        listOf(
            OnBoard(R.drawable.image_onboarding_curation, R.string.ob_title1, R.string.ob_sub1),
            OnBoard(R.drawable.image_onboarding_detail, R.string.ob_title2, R.string.ob_sub2),
            OnBoard(R.drawable.image_onboarding_chatbot, R.string.ob_title3, R.string.ob_sub3),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("pref_onboarding", MODE_PRIVATE)

        viewPager = binding.vpOnBoarding
        adapter = OnBoardAdapter(pages)
        viewPager.adapter = adapter

        binding.dotsIndicator.setViewPager2(viewPager)

        binding.btnNext.setOnClickListener { handleNext() }
        binding.tvSkip.setOnClickListener { finishOnBoarding() }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pages.lastIndex) {
                    binding.btnNext.text = getString(R.string.onboarding_start)
                    binding.tvSkip.visibility = View.INVISIBLE    // ← 건너뛰기 숨김
                } else {
                    binding.btnNext.text = getString(R.string.onboarding_next)
                    binding.tvSkip.visibility = View.VISIBLE      // ← 다시 보이게
                }
            }
        })

    }

    private fun handleNext() {
        if (viewPager.currentItem < pages.lastIndex) {
            viewPager.currentItem += 1
        } else {
            finishOnBoarding()
        }
    }

    private fun finishOnBoarding() {
        prefs.edit { putBoolean("completed", true) }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
