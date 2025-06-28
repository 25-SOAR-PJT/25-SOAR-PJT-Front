package com.example.soar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.soar.databinding.ActivityDetailPageBinding

class DetailPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_page)

        binding = ActivityDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}