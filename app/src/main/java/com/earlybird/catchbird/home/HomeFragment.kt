package com.earlybird.catchbird.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.earlybird.catchbird.*
import com.earlybird.catchbird.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    handleImage(it.data)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        with(binding) {
            imgBtn.setOnClickListener {
                getFromAlbum()
            }
            modelBtn.setOnClickListener {
                val bitmapDrawable = imageView.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                val model = ClassificationModel(requireContext())
                output.text = model.execution(bitmap)
            }

        }

        return binding.root
    }
    fun getFromAlbum() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        activityLauncher.launch(intent)
    }

    @SuppressLint("Recycle")
    fun handleImage(data: Intent?) {
        val uri = data?.data
        Glide.with(requireActivity()).load(uri).into(binding.imageView)
    }

    @SuppressLint("Range")
    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        val cursor = requireActivity().contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }

        return path
    }
}