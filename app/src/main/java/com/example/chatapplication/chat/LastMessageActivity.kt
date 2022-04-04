package com.example.chatapplication.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityLastMessageBinding

class LastMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLastMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_message)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_last_message)
    }
}