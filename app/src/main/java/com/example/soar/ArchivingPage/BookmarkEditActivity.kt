package com.example.soar.ArchivingPage

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.Network.archiving.BookmarkedPolicy
import com.example.soar.R
import com.example.soar.databinding.ActivityBookmarkEditBinding
import com.example.soar.util.showBlockingToast // ✨ 1. Import the custom toast utility

class BookmarkEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkEditBinding
    private val viewModel: BookmarkEditViewModel by viewModels()
    private lateinit var adapter: BookmarkEditAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupRecyclerView()
        setupListeners()
        setupObservers()

        val policies = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("ALL_POLICIES", BookmarkedPolicy::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("ALL_POLICIES")
        }
        policies?.let { viewModel.setPolicies(it) }
    }

    private fun setupAppBar() {
        binding.appbar.textTitle.text = getString(R.string.edit2)
        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = BookmarkEditAdapter { policyId ->
            viewModel.toggleSelection(policyId)
        }
        binding.recentSearchContainer.adapter = adapter
        binding.recentSearchContainer.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.selectAll.setOnClickListener {
            viewModel.toggleSelectAll()
        }
        binding.btnApplyComplete.setOnClickListener {
            viewModel.applyForSelectedPolicies()
        }
        binding.btnDelete.setOnClickListener {
            viewModel.deleteSelectedPolicies()
        }
    }

    private fun setupObservers() {
        viewModel.uiModels.observe(this) { models ->
            adapter.submitList(models)
            binding.num.text = models.size.toString()
        }

        viewModel.selectedCount.observe(this) { count ->
            updateBottomButtons(count > 0)
            val allItemCount = viewModel.uiModels.value?.size ?: 0
            if (count > 0 && count == allItemCount) {
                binding.selectAll.text = getString(R.string.deselect_all)
            } else {
                binding.selectAll.text = getString(R.string.select_all)
            }
        }

        // ✨ 2. Observe LiveData for toast messages
        viewModel.toastMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                // ✨ 3. Replace standard Toast with the custom TouchBlockingToast
                // The cancel button is hidden because no undo action is needed here.
                showBlockingToast(message, hideCancel = true)
            }
        }
    }

    private fun updateBottomButtons(isEnabled: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.ref_white)
        val inactiveColor = ContextCompat.getColor(this, R.color.ref_coolgray_400)

        binding.btnApplyComplete.isEnabled = isEnabled
        binding.btnDelete.isEnabled = isEnabled

        if (isEnabled) {
            binding.btnImg1.setColorFilter(activeColor)
            binding.btnText1.setTextColor(activeColor)
            binding.btnImg2.setColorFilter(activeColor)
            binding.btnText2.setTextColor(activeColor)
        } else {
            binding.btnImg1.setColorFilter(inactiveColor)
            binding.btnText1.setTextColor(inactiveColor)
            binding.btnImg2.setColorFilter(inactiveColor)
            binding.btnText2.setTextColor(inactiveColor)
        }
    }
}