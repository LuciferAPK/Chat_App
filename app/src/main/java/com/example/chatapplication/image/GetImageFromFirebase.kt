package com.example.chatapplication.image

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GetImageFromFirebase {
    private val user by lazy {
        FirebaseAuth.getInstance().currentUser
    }
    private lateinit var getLinkImageInFirebaseDatabase: Uri

    fun getImageFromFirebaseDatabase(imageView: ImageView, context: Context) {
        val getUrlDTB = FirebaseDatabase.getInstance().reference.child("/user/${user?.uid}/imgUrl")
        getUrlDTB.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val uriToString: String = snapshot.value.toString()
                getLinkImageInFirebaseDatabase = Uri.parse(uriToString)
                if (snapshot.value == null) {
                    if (user?.photoUrl != null) {
                        Glide.with(context)
                            .load(user?.photoUrl)
                            .centerCrop()
                            .into(imageView)
                    }
                }
                else {
                    Glide.with(context)
                        .load(getLinkImageInFirebaseDatabase)
                        .centerCrop()
                        .into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}