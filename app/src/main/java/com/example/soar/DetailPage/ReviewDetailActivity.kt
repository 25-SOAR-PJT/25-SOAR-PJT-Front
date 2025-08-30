package com.example.soar.DetailPage

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import com.example.soar.Network.detail.CommentResponse
import com.example.soar.R
import com.example.soar.databinding.ActivityReviewDetailBinding

class ReviewDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewDetailBinding
    private val viewModel: CommentViewModel by viewModels()
    private lateinit var adapter: ReviewAdapter

    private var policyId: String? = null
    private var editingComment: CommentResponse? = null // 수정 중인 댓글 정보 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        policyId = intent.getStringExtra("policyId")
        if (policyId == null) {
            Toast.makeText(this, "정책 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupAppBar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadComments(policyId!!)

        // DetailPage에서 입력창을 바로 활성화하라는 요청이 있었는지 확인
        if (intent.getBooleanExtra("FOCUS_INPUT", false)) {
            showSendUI()
        }
    }

    private fun setupAppBar() {
        binding.appbar.textTitle.text = getString(R.string.review)
        binding.appbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter { comment ->
            // 어댑터에서 ... 버튼 클릭 시 호출될 람다
            val bottomSheet = ReviewBottomFragment(
                onEditRequest = { showEditUI(comment) },
                onDeleteRequest = { viewModel.removeComment(comment.commentId) }
            )
            bottomSheet.show(supportFragmentManager, "ReviewBottomFragment")
        }
        binding.reviewRecyclerview.adapter = adapter
        binding.reviewRecyclerview.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        // '댓글을 입력해주세요' 텍스트뷰 클릭 시
        binding.textInput1.setOnClickListener {
            showSendUI()
        }

        // '전송' 버튼 클릭 시
        binding.btnSend.setOnClickListener {
            val commentText = binding.textInput2.text.toString().trim()
            if (commentText.isNotEmpty()) {
                viewModel.addComment(policyId!!, commentText)
            } else {
                Toast.makeText(this, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // '수정' 버튼 클릭 시
        binding.btnEdit.setOnClickListener {
            val updatedText = binding.textInput2.text.toString().trim()
            editingComment?.let { commentToEdit -> // 변수명을 명확하게 변경
                if (updatedText.isNotEmpty()) {
                    // policyId를 함께 전달하도록 수정
                    viewModel.editComment(commentToEdit.commentId, commentToEdit.policyId, updatedText)
                } else {
                    Toast.makeText(this, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun observeViewModel() {
        viewModel.comments.observe(this) { comments ->
            adapter.submitList(comments.toMutableList()) // 항상 새로운 리스트를 전달하여 DiffUtil이 동작하도록 함
            binding.textReviewCount.text = comments.size.toString()
        }
        viewModel.toastEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // API 호출 성공 메시지일 경우, 입력창과 키보드를 숨김
                if (message.contains("등록") || message.contains("수정") || message.contains("삭제")) {
                    hideKeyboardAndResetInput()
                }
            }
        }
    }

    // '전송' 모드로 UI 변경 및 키보드 표시
    private fun showSendUI() {
        binding.textInput1.visibility = View.GONE
        binding.commentInputLayout.visibility = View.VISIBLE
        binding.btnSend.visibility = View.VISIBLE
        binding.btnEditBox.visibility = View.GONE

        binding.textInput2.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.textInput2, InputMethodManager.SHOW_IMPLICIT)
    }

    // '수정' 모드로 UI 변경 및 키보드 표시
    private fun showEditUI(comment: CommentResponse) {
        editingComment = comment // 수정할 댓글 정보 저장

        binding.textInput1.visibility = View.GONE
        binding.commentInputLayout.visibility = View.VISIBLE
        binding.btnSend.visibility = View.GONE
        binding.btnEditBox.visibility = View.VISIBLE

        binding.textInput2.setText(comment.comment)
        binding.textInput2.requestFocus()
        binding.textInput2.setSelection(comment.comment.length) // 커서를 텍스트 끝으로 이동

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.textInput2, InputMethodManager.SHOW_IMPLICIT)
    }

    // 키보드 숨기고 입력창 관련 UI를 초기 상태로 복원
    private fun hideKeyboardAndResetInput() {
        // 1. 키보드 숨기기
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        // 2. UI 상태 초기화
        binding.textInput2.clearFocus()
        binding.textInput2.setText("")
        binding.commentInputLayout.visibility = View.GONE
        binding.textInput1.visibility = View.VISIBLE

        editingComment = null // 수정 상태 초기화
    }
}