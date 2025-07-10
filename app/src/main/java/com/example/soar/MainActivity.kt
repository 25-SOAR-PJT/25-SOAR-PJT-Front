package com.example.soar

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.soar.ArchivingPage.ArchivingFragment
import com.example.soar.CalendarPage.CalendarFragment
import com.example.soar.ExplorePage.ExploreFragment
import com.example.soar.HomePage.HomeFragment
import com.example.soar.MyPage.MypageFragment
import com.example.soar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()

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

    private fun updateNavSelection(selected: View) {
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

}