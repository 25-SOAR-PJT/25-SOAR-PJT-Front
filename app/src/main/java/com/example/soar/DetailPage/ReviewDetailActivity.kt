package com.example.soar.DetailPage

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soar.R
import com.example.soar.databinding.ActivityReviewDetailBinding
import org.w3c.dom.Comment

class ReviewDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewDetailBinding

    private val reviews = loadReviews().toMutableList()
    private lateinit var adapter: ReviewAdapter
    private var editingPosition: Int? = null

    //리뷰 데이터 가져오기
    fun loadReviews(): List<Item.Review> {
        return DummyData.reviewItems
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 바
        val textTitle = findViewById<TextView>(R.id.text_title)
        textTitle.text = getString(R.string.review)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 댓글 표시
        binding.reviewRecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(
            reviews,
            onOptionsClick = { position ->
                val review = reviews[position]

                val bottomSheet = ReviewBottomFragment (
                    onEditRequest = {
                        showEditUI(review, position)
                    },
                    onDeleteRequest = {
                        deleteComment(position)
                    })
                bottomSheet.show(supportFragmentManager, "ReviewBottomFragment")
            },
            showOptions = true
        )
        binding.reviewRecyclerview.adapter = adapter
        val count = if (reviews.size > 999) "999+" else reviews.size.toString()
        binding.textReviewCount.text = count

        // 새 댓글 입력
        val shouldFocus = intent.getBooleanExtra("FOCUS_INPUT", false)
        if (shouldFocus) {
            showSendUI()
            binding.textInput2.requestFocus()
            // 키보드 자동 표시
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.textInput2, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.textInput1.setOnClickListener {
            showSendUI()
            binding.textInput1.visibility = View.GONE

            binding.textInput2.requestFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.textInput2, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.btnSend.setOnClickListener {
            val newCommentText = binding.textInput2.text.toString().trim()

            if (newCommentText.isNotEmpty()) {
                // 임시로 유저 이름 익명
                val newReview = Item.Review("익명", newCommentText)
                reviews.add(0, newReview)
                adapter.notifyItemInserted(0)
                binding.reviewRecyclerview.scrollToPosition(0)

                // 입력창 초기화
                binding.textInput2.setText("")
                binding.commentInputLayout.visibility = View.GONE
                binding.btnSend.visibility = View.GONE

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.textInput1.windowToken, 0)

                Toast.makeText(this, getString(R.string.toast_add_review), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.toast_review_error), Toast.LENGTH_SHORT).show()
            }
            binding.textInput1.visibility = View.VISIBLE
        }


        // 댓글 수정
        binding.btnEdit.setOnClickListener {
            val updatedText = binding.textInput2.text.toString().trim()
            editingPosition?.let { position ->
                if (updatedText.isNotEmpty()) {
                    reviews[position] = reviews[position].copy(content = updatedText)
                    adapter.notifyItemChanged(position)

                    // UI 초기화
                    binding.commentInputLayout.visibility = View.GONE
                    binding.btnEditBox.visibility = View.GONE
                    binding.textInput2.setText("")

                    Toast.makeText(this, getString(R.string.toast_edit_review), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.toast_review_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    fun showSendUI() {
        binding.commentInputLayout.visibility = View.VISIBLE
        binding.btnSend.visibility = View.VISIBLE
        binding.btnEditBox.visibility = View.GONE
    }

    fun showEditUI(review: Item.Review, position: Int) {
        binding.commentInputLayout.visibility = View.VISIBLE
        binding.btnSend.visibility = View.GONE
        binding.btnEditBox.visibility = View.VISIBLE

        binding.textInput2.setText(review.content)

        binding.textInput2.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.textInput2, InputMethodManager.SHOW_IMPLICIT)

        editingPosition = position
    }

    fun deleteComment(position: Int) {
        reviews.removeAt(position)
        adapter.notifyItemRemoved(position)
        Toast.makeText(this, getString(R.string.toast_delete_review), Toast.LENGTH_SHORT).show()
    }
}