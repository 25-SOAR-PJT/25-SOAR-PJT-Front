package com.example.soar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.EntryPage.Splash.SplashPageActivity
import com.example.soar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn1.setOnClickListener {
            // Splash 를 경유해 Detail 로
            val intent = Intent(this, SplashPageActivity::class.java).apply {
                putExtra("NEXT_CLASS", DetailPageActivity::class.java.name)
            }
            startActivity(intent)
        }
    }
}