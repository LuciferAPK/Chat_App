package com.example.chatapplication.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityLogInBinding
import com.example.chatapplication.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private val progressDialog by lazy {
        ProgressDialog(this)
    }
    private val mAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_log_in)
        clickLogin()
        clickSignUp()
    }

    fun clickLogin() {
        binding.btnLogin.setOnClickListener {
            if (binding.lqEmail.text.toString().isNotEmpty() &&
                binding.lgPassword.text.toString().isNotEmpty()
            ) {
                loGinUser(binding.lqEmail.text.toString(), binding.lgPassword.text.toString())
            } else {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loGinUser(email: String, pass: String) {
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
//                    finishAffinity()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Sai tài khoản hoặc mật khẩu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun clickSignUp() {
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}