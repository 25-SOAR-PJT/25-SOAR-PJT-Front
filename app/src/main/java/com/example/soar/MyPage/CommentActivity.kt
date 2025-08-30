// MyPage/CommentActivity.kt
package com.example.soar.MyPage

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.R
import com.example.soar.databinding.ActivityCommentBinding

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
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        // TODO: isLoading LiveData를 관찰하여 로딩 인디케이터 표시
    }
}