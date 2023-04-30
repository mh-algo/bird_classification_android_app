package com.earlybird.catchbird.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.earlybird.catchbird.ClassificationModel
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.FragmentShowImageBinding
import java.io.FileNotFoundException
import java.io.InputStream

class ShowImageFragment : Fragment() {
    private val binding: FragmentShowImageBinding by lazy {
        FragmentShowImageBinding.inflate(layoutInflater)
    }

    lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private var uri: Uri? = null
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        type = bundle?.getString("type")?:""
        val path = bundle?.getString("path")
        uri = Uri.parse(path)

        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    handleImage(it.data)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Glide.with(requireActivity()).asBitmap().load(uri).into(binding.imageView)

        with(binding) {
            when(type) {
                "image" -> againBtn.text = "사진 다시 선택하기"
            }

            againBtn.setOnClickListener {
                when(type){
                    "camera" ->
                        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, CameraFragment()).commit()
                    "image" -> getFromAlbum()
                }

            }
            modelBtn.setOnClickListener {
                var imageStream: InputStream? = null
                try {
                    imageStream = requireContext().contentResolver.openInputStream(uri!!)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
                imageStream?.close()

                val model = ClassificationModel(requireContext())
                val chkBird: String = model.execution(bitmap, "bird")   // 새인지 아닌지 구별
                if (chkBird.toInt() == 1) {     // 새인 경우
                    showModelResultFragment(uri, type)
                } else {
                    Toast.makeText(requireContext(), "새를 식별할 수 없습니다.\n사진을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            backBtn.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, CameraFragment()).commit()
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance(path: String?, type: String?): ShowImageFragment {
            val fragment = ShowImageFragment()

            val bundle = Bundle()
            bundle.putString("type", type)
            bundle.putString("path", path)
            fragment.arguments = bundle

            return fragment
        }
    }

    private fun getFromAlbum() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        activityLauncher.launch(intent)
    }

    @SuppressLint("Recycle")
    fun handleImage(data: Intent?) {
        uri = data?.data
        Glide.with(requireActivity()).asBitmap().load(uri).into(binding.imageView)
    }

    private fun showModelResultFragment(imageUri: Uri?, type: String?) {
        val fragment = ModelResultFragment.newInstance(imageUri.toString(), type)
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment).commit()
    }
}