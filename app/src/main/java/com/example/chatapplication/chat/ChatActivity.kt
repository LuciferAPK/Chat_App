package com.example.chatapplication.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.image.GetImageFromFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val mAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var mDbRef: DatabaseReference
//    private val mDbRef by lazy {
//        FirebaseDatabase.getInstance().reference
//    }
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private var receiverRoom: String? = null
    private var senderRoom: String? = null
//    private val getImageFromFirebase by lazy {
//        GetImageFromFirebase()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        mDbRef = FirebaseDatabase.getInstance().reference

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = mAuth.currentUser?.uid

        senderRoom = "$receiverUid--to--$senderUid"
        receiverRoom = "$senderUid--to--$receiverUid"

//        getImageFromFirebase.getImageFromFirebaseDatabase(binding.imgAvt, this)
        binding.toolbar.title = "$name"

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        binding.chatRecycleview.layoutManager = LinearLayoutManager(this)
        binding.chatRecycleview.adapter = messageAdapter

        //logic for adding data to recycleView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecycleview.smoothScrollToPosition(messageList.size)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        //add message to dtb
        binding.sentBt.setOnClickListener {
            val message = binding.messageBox.text.toString()
            val messageObject = Message(message, senderUid)

            mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            binding.messageBox.setOnClickListener {
                binding.chatRecycleview.smoothScrollToPosition(messageList.size)
//                Log.d("TAG", "vai ca")
            }
            binding.chatRecycleview.smoothScrollToPosition(messageList.size)
            binding.messageBox.text = null
        }
    }
}