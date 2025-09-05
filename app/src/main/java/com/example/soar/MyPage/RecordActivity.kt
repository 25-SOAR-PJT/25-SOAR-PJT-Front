package com.example.soar.MyPage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.DetailPage.DetailPageActivity
import com.example.soar.ExplorePage.ExploreAdapter
import com.example.soar.MainActivity
import com.example.soar.Network.RecentViewManager
import com.example.soar.Network.explore.YouthPolicy
import com.example.soar.databinding.ActivityRecordBinding
import com.example.soar.util.showBlockingToast

class RecordActivity : AppCompatActivity(), ExploreAdapter.OnItemClickListener {
    private lateinit var binding: ActivityRecordBinding
    private val viewModel: RecordViewModel by viewModels()
    private lateinit var adapter: ExploreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupRecyclerView()
        setupObservers()

        // RecentViewManager에서 ID 목록 가져오기
        val recentPolicyIds = RecentViewManager.getRecentPolicies()

        if (recentPolicyIds.isEmpty()) {
            // 기록이 없을 경우: 안내 문구 표시
            binding.btnZeroEntry.visibility = View.VISIBLE
            binding.bizList.visibility = View.GONE
            binding.btnToExplore.setOnClickListener {
                // MainActivity로 이동하기 위한 Intent 생성
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("start_destination", "explore")
                }
                startActivity(intent)
            }
        } else {
            // 기록이 있을 경우: 안내 문구 숨기고, ViewModel을 통해 데이터 로드
            binding.btnZeroEntry.visibility = View.GONE
            binding.bizList.visibility = View.VISIBLE
            viewModel.loadRecentPolicies(recentPolicyIds)
        }
    }

    private fun setupAppBar() {
        binding.appbar.textTitle.text = "최근 본 지원 사업" // 제목 변경
        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // ExploreAdapter 재사용. 단, 기록 화면에서는 북마크 기능을 비활성화
        adapter = ExploreAdapter(this, isBookmarkEnabled = false)
        binding.bizList.adapter = adapter
        binding.bizList.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.policies.observe(this) { policies ->
            adapter.submitList(policies)
        }
        viewModel.error.observe(this) { error ->
            showBlockingToast(error, hideCancel = true)
        }
        // TODO: isLoading 상태에 따라 ProgressBar 표시/숨김 처리
    }

    // ExploreAdapter.OnItemClickListener 인터페이스 구현
    override fun onPolicyItemClick(policy: YouthPolicy) {
        val intent = Intent(this, DetailPageActivity::class.java).apply {
            putExtra("policyId", policy.policyId)
        }
        startActivity(intent)
    }

    override fun onBookmarkClick(policy: YouthPolicy) {
        // 이 화면에서는 북마크 기능이 비활성화되어 호출되지 않음
    }
}