package com.example.soar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.soar.ArchivingPage.ArchivingFragment
import com.example.soar.CalendarPage.CalendarFragment
import com.example.soar.ExplorePage.ExploreFragment
import com.example.soar.HomePage.HomeFragment
import com.example.soar.MyPage.MypageFragment
import com.example.soar.Network.RecentViewManager
import com.example.soar.Network.TokenManager
import com.example.soar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val openFragment = intent.getStringExtra("openFragment")
        Log.d("MainActivity", "openFragment value: $openFragment")
        if (openFragment == "archivingFragment") {
            Log.d("MainActivity", "Opening ArchivingFragment")
            // ArchivingFragment를 여는 코드
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ArchivingFragment())
                .addToBackStack(null)
                .commit()
        }else{
            RecentViewManager.init(this)
        }


        chooseStartDestination(intent)


        // nav 버튼 클릭 시 Fragment 교체
        binding.navHome.setOnClickListener {
            updateNavSelection(binding.navHome)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
        binding.navExplore.setOnClickListener {
            updateNavSelection(binding.navExplore)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ExploreFragment())
                .commit()
        }
        binding.navArchiving.setOnClickListener {
            updateNavSelection(binding.navArchiving)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ArchivingFragment())
                .commit()
        }
        binding.navCalendar.setOnClickListener {
            updateNavSelection(binding.navCalendar)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CalendarFragment())
                .commit()
        }
        binding.navMypage.setOnClickListener {
            updateNavSelection(binding.navMypage)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MypageFragment())
                .commit()
        }

    }

    fun updateNavSelection(selected: View) {
        val navItems = listOf(
            Triple(binding.navHome, binding.imgHome, binding.textHome),
            Triple(binding.navExplore, binding.imgExplore, binding.textExplore),
            Triple(binding.navArchiving, binding.imgArchiving, binding.textArchiving),
            Triple(binding.navCalendar, binding.imgCalendar, binding.textCalendar),
            Triple(binding.navMypage, binding.imgMypage, binding.textMypage)
        )

        for ((layout, imageView, textView) in navItems) {
            val colorRes = if (layout == selected) R.color.ref_coolgray_600 else R.color.ref_coolgray_200
            imageView.setColorFilter(ContextCompat.getColor(this, colorRes), android.graphics.PorterDuff.Mode.SRC_IN)
            textView.setTextColor(ContextCompat.getColor(this, colorRes))
        }
    }

    fun updateNavByTag(tag: String) {
        when (tag) {
            "home" -> updateNavSelection(binding.navHome)
            "explore" -> updateNavSelection(binding.navExplore)
            "archiving" -> updateNavSelection(binding.navArchiving)
            "calendar" -> updateNavSelection(binding.navCalendar)
            "mypage" -> updateNavSelection(binding.navMypage)
        }
    }

    fun goToExploreTab() {
        updateNavSelection(binding.navExplore)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ExploreFragment())
            .commit()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        chooseStartDestination(intent)
    }

    private fun chooseStartDestination(intent: Intent?) {
        val dest = intent?.getStringExtra("start_destination")
        when (dest) {
            "explore" -> {
                updateNavSelection(binding.navExplore)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ExploreFragment())
                    .commit()
            }

            "archiving" -> {
                updateNavSelection(binding.navArchiving)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ArchivingFragment())
                    .commit()
            }
            else -> {
                updateNavSelection(binding.navHome)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, HomeFragment())
                    .commit()
            }
        }

        val shouldOpenPersonalBiz = intent?.getBooleanExtra("open_personal_biz", false) == true
        if (shouldOpenPersonalBiz) {
            // 프래그먼트 트랜잭션 적용 직후 실행되도록 post
            binding.container.post {
                val accessToken = TokenManager.getAccessToken()
                if (!accessToken.isNullOrEmpty()) {
                    startActivity(Intent(this, com.example.soar.ExplorePage.PersonalBizActivity::class.java))
                    // Explore 위로 바텀시트처럼 애니메이션
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
                }
                // 비로그인이라면 여기서 로그인 페이지로 유도하도록 분기해도 됨
            }
        }
    }


}