package com.example.soar.EntryPage.SignIn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.soar.MainActivity
import com.example.soar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageButton
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_login_page)

        btnClose = findViewById(R.id.btnClose)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validate()
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }
        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)

        // ‘완료’ 키 눌렀을 때 로그인 시도
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && btnLogin.isEnabled) {
                submitLogin()
                true
            } else false
        }

        btnLogin.setOnClickListener { submitLogin() }
        btnClose.setOnClickListener { submitClose() }
    }

    private fun validate() {
        // 1) 이메일 형식
        val email = etEmail.text.toString()
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        // 2) 비밀번호 8자 이상 예시
        val pw = etPassword.text.toString()
        val pwOk = pw.length >= 8

        // 에러 메시지 처리
        tilEmail.error = if (email.isEmpty() || emailOk) null
        else getString(R.string.error_email)

        tilPassword.error = if (pw.isEmpty() || pwOk) null
        else getString(R.string.error_password)

        // 버튼 활성
        btnLogin.isEnabled = emailOk && pwOk
    }

    private fun submitLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun submitClose() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
