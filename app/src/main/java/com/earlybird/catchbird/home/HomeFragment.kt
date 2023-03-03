package com.earlybird.catchbird.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.earlybird.catchbird.*
import com.earlybird.catchbird.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
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
                val bitmapDrawable = ImageView.drawable as BitmapDrawable
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
        startActivityForResult(intent, 102)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            102 -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data)
                }
            }
        }
    }

    fun handleImage(data: Intent?) {
        var imagePath: String? = null
        val uri = data?.data

        if (uri != null) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val id = docId.split(":")[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection
                )
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                imagePath = getImagePath(uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imagePath = uri.path
            }

            val bitmap = BitmapFactory.decodeFile(imagePath)
            binding.ImageView.setImageBitmap(bitmap)
        }
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