package com.example.chatapplication.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivitySplashBinding
import com.example.chatapplication.login.LoginActivity
import com.example.chatapplication.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val mAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        animation()
        timeDelay()
    }

    private fun animation() {
        binding.logo.animation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        binding.textLogo.animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        binding.textBio.animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
    }

    private fun timeDelay() {
        Handler().postDelayed({
            nextActivity()
        }, 2000)
    }

    private fun nextActivity() {
        if (mAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}