package com.example.chatapplication.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.R
import com.example.chatapplication.chat.LastMessageActivity
import com.example.chatapplication.databinding.ActivityMainBinding
import com.example.chatapplication.databinding.LayoutHeaderNavBinding
import com.example.chatapplication.image.GetImageFromFirebase
import com.example.chatapplication.image.ImagePickerFragment
import com.example.chatapplication.login.LoginActivity
import com.example.chatapplication.user.User
import com.example.chatapplication.user.UserAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val Camera_Code = 123
        const val Library_Code = 456
    }

    private val imagePickerFragment by lazy {
        ImagePickerFragment(
            onCameraClicked = { clickCamera() },
            onLibraryClicked = { clickLibrary() })
    }
    private lateinit var getNameFromFirebase: String
    private lateinit var binding: ActivityMainBinding
    private lateinit var userList: ArrayList<User>
    private var selectedPhotoUri: Uri? = null
    private lateinit var adapter: UserAdapter
    private val mDbRef by lazy {
        FirebaseDatabase.getInstance().reference
    }
    private val headerNavBinding by lazy {
        LayoutHeaderNavBinding.bind(binding.navigationView.getHeaderView(0))
    }

    //action bar
    private val actionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_drawer_open,
            R.string.nav_drawer_close
        )
    }
    private val mAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val getImageFromFirebase by lazy {
        GetImageFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        getNameFromFirebase = ""
        getImageFromFirebase.getImageFromFirebaseDatabase(headerNavBinding.imgAvt, this)

        userList = ArrayList()
        adapter = UserAdapter(userList)
        binding.userRecycleView.layoutManager = LinearLayoutManager(this)
        binding.userRecycleView.adapter = adapter
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        selectedCamera()
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //navigationView
        binding.navigationView.setNavigationItemSelectedListener(this)
        getUserInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_user -> {
                Toast.makeText(this, "Tíng năng hiện đang bảo trì", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_tn -> {
                val intent = Intent(this, LastMessageActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
            }
            R.id.nav_mk -> {
                Toast.makeText(this, "Tíng năng hiện đang bảo trì", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_lo -> {
                mAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Camera_Code) {
            val pic = data?.getParcelableExtra<Bitmap>("data")
            if (pic != null) {
                headerNavBinding.imgAvt.setImageURI(getImageUriFromBitmap(this, pic))
            }
            uploadImage(getImageUriFromBitmap(this, pic!!))
        }

        if (requestCode == Library_Code) {
            selectedPhotoUri = data?.data
            if (selectedPhotoUri != null) {
                headerNavBinding.imgAvt.setImageURI(selectedPhotoUri)
            }
            uploadImage(selectedPhotoUri!!)
        }
    }

    private fun clickCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Camera_Code)
        imagePickerFragment.dismiss()
    }

    private fun clickLibrary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Library_Code)
        imagePickerFragment.dismiss()
    }

    //convert Bitmap to Uri
    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    private fun selectedCamera() {
        headerNavBinding.camera.setOnClickListener {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 2507
            )
            imagePickerFragment.show(supportFragmentManager, null)
        }
    }

    private fun uploadImage(uri: Uri) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().reference.child("/images/$filename")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                updateUser(it)
            }
        }.addOnFailureListener {

        }
    }

    private fun updateUser(image: Uri) {
        val uid = FirebaseAuth.getInstance().uid
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/user")
        val update = mapOf<String, String>(
            "name" to getNameFromFirebase,
            "email" to email,
            "imgUrl" to image.toString(),
            "uid" to uid.toString()
        )
        ref.child(uid!!).updateChildren(update).addOnSuccessListener {

        }.addOnFailureListener {

        }
    }

    private fun getUserInfo() {
        val getName =
            FirebaseDatabase.getInstance().reference.child("user/${mAuth.currentUser?.uid}/name")
        getName.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getNameToString: String = snapshot.value.toString()
                getNameFromFirebase = getNameToString
                val email = mAuth.currentUser?.email
                headerNavBinding.tvEmail.text = email
                headerNavBinding.tvName.text = getNameToString
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}