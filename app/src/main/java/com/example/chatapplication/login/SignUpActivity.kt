package com.example.chatapplication.login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivitySignUpBinding
import com.example.chatapplication.image.ImagePickerFragment
import com.example.chatapplication.main.MainActivity
import com.example.chatapplication.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class SignUpActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EmailPassword"
    }

    private val imagePickerFragment by lazy {
        ImagePickerFragment(
            onCameraClicked = { clickCamera() },
            onLibraryClicked = { clickLibrary() })
    }
    private var count = 0
    private var imageCheck: Uri? = null
    private var selectedPhotoUri: Uri? = null
    private lateinit var binding: ActivitySignUpBinding
    private val mAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val  progressDialog by lazy {
        ProgressDialog(this)
    }
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        selectedCamera()
        clickSignUp()
        clickCancel()
    }

    private fun clickSignUp() {
        binding.btnSignup.setOnClickListener {
            if (binding.suEmail.text?.trim().toString().isNotEmpty()
                && binding.suPassword.text?.trim().toString().isNotEmpty()
                && binding.suCfpassword.text?.trim().toString().isNotEmpty()
            ) {
                if (!isEmailValid(binding.suEmail.text?.trim().toString())) {
                    Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                } else {
                    if (binding.suPassword.text?.trim()
                            .toString() != binding.suCfpassword.text?.trim().toString()
                    ) {
                        Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                    } else {
                        if (binding.suPassword.text?.trim().toString().length <= 5
                            || binding.suCfpassword.text?.trim().toString().length <= 5
                        ) {
                            Toast.makeText(
                                this, "Mật khẩu phải có ít nhất 6 kí tự", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if(count == 0) {
                                createAccount(
                                    binding.userName.text?.trim().toString(),
                                    binding.suEmail.text?.trim().toString(),
                                    binding.suPassword.text?.trim().toString(),
                                    ""
                                )
                            } else {
                                createAccount(
                                    binding.userName.text?.trim().toString(),
                                    binding.suEmail.text?.trim().toString(),
                                    binding.suPassword.text?.trim().toString(),
                                    imageCheck.toString()
                                )
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Vui lòng kiểm tra lại thông tin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAccount(name: String, email: String, password: String, imgUrl: String) {
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    // Sign in success, update UI with the signed-in user's information
                    addUserToDatabase(name, email, mAuth.uid!!, imgUrl)
                    val intent = Intent(this, MainActivity::class.java)
//                    intent.putExtra("user_name", binding.userName.text?.trim().toString())
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    progressDialog.dismiss()
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val check = task.exception is FirebaseAuthUserCollisionException
                    if (check) {
                        Toast.makeText(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            baseContext, "Xác thực thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String, imgUrl: String) {
        mDbRef = FirebaseDatabase.getInstance().reference
        mDbRef.child("user").child(uid).setValue(User(name, email, uid, imgUrl))
    }

    private fun selectedCamera() {
        binding.camera.setOnClickListener {
            count++
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.Camera_Code) {
            val pic = data?.getParcelableExtra<Bitmap>("data")
            if (pic != null) {
//                imageCheck = getImageUriFromBitmap(this, pic)
                binding.imageProfile.setImageURI(getImageUriFromBitmap(this, pic))
            }
            uploadImage(getImageUriFromBitmap(this, pic!!))
        }

        if (requestCode == MainActivity.Library_Code) {
            selectedPhotoUri = data?.data
            if (selectedPhotoUri != null) {
//                imageCheck = selectedPhotoUri as Uri
                binding.imageProfile.setImageURI(selectedPhotoUri)
            }
            uploadImage(selectedPhotoUri!!)
        }
    }

    private fun uploadImage(uri: Uri) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().reference.child("/images/$filename")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                imageCheck = it
//                Log.d("TAG", "uploadImage: $checker")
            }
        }.addOnFailureListener {

        }
    }

    private fun clickCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, MainActivity.Camera_Code)
        imagePickerFragment.dismiss()
    }

    private fun clickLibrary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, MainActivity.Library_Code)
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

    private fun clickCancel() {
        binding.tvCancle.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    //check mail
    private fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
    }
}