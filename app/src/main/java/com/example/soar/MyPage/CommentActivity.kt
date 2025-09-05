// MyPage/CommentActivity.kt
package com.example.soar.MyPage

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.R
import com.example.soar.databinding.ActivityCommentBinding
import com.example.soar.util.showBlockingToast

class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding
    private val viewModel: MyCommentViewModel by viewModels()
    private lateinit var adapter: MyCommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchMyComments()
    }


    private fun setupAppBar() {
        binding.appbar.textTitle.text = getString(R.string.my_comment)
        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = MyCommentAdapter()
        binding.myComment.adapter = adapter
        binding.myComment.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.myComments.observe(this) { comments ->
            adapter.submitList(comments)
            binding.commentNum.text = comments.size.toString()

            if (comments.isEmpty()) {
                binding.myComment.visibility = View.GONE
                binding.btnZeroEntry.visibility = View.VISIBLE
            } else {
                binding.myComment.visibility = View.VISIBLE
                binding.btnZeroEntry.visibility = View.GONE
            }
        }

        viewModel.error.observe(this) { error ->
            showBlockingToast(error, hideCancel = true)
        }

        // TODO: isLoading LiveData를 관찰하여 로딩 인디케이터 표시
    }
}