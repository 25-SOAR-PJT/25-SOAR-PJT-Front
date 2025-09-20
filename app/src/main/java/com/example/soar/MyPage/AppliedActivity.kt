package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.MainActivity
import com.example.soar.R
import com.example.soar.databinding.ActivityAppliedBinding

class AppliedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppliedBinding
    private val viewModel: AppliedViewModel by viewModels()
    private lateinit var adapter: AppliedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppliedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // 화면에 다시 돌아올 때마다 데이터를 새로고침하여 최신 상태 유지
        viewModel.fetchBookmarkedPolicies()
    }

    private fun setupAppBar() {
        binding.appbar.textTitle.text = getString(R.string.applied)
        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = AppliedAdapter()
        binding.appliedBiz.adapter = adapter
        binding.appliedBiz.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.appliedPolicies.observe(this) { allPolicies ->
            adapter.submitList(allPolicies)
        }

        viewModel.policyCounts.observe(this) { policyCounts ->
            binding.appliedCounts.text =
                getString(R.string.pb_suggestion_count, policyCounts)
            if(policyCounts == 0){
                binding.btnZeroEntry.visibility = View.VISIBLE
                binding.appliedContainer.visibility = View.GONE
                binding.appliedBiz.visibility = View.GONE
                // ✨ 추가: "둘러보기" 버튼 클릭 리스너 설정
                binding.btnToExplore.setOnClickListener {
                    // MainActivity로 이동하기 위한 Intent 생성
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("start_destination", "archiving")
                    }
                    startActivity(intent)
                }
            }
            else{
                binding.btnZeroEntry.visibility = View.GONE
                binding.appliedContainer.visibility = View.VISIBLE
                binding.appliedBiz.visibility = View.VISIBLE
            }
        }



        // TODO: isLoading, error LiveData를 관찰하여 로딩 인디케이터나 에러 메시지 표시
    }
}