package com.example.chatapplication.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.chatapplication.R
import com.example.chatapplication.databinding.FragmentImagePickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerFragment(
    private val onLibraryClicked: () -> Unit,
    private val onCameraClicked: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentImagePickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_image_picker,
            container,
            false
        )
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.chooseCamera.setOnClickListener { onCameraClicked() }
        binding.chooseLibrary.setOnClickListener { onLibraryClicked() }
        return binding.root
    }
}