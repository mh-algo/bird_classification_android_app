package com.earlybird.catchbird.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
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

class ShowImageFragment : Fragment() {
    private val binding: FragmentShowImageBinding by lazy {
        FragmentShowImageBinding.inflate(layoutInflater)
    }

    lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private var uri: Uri? = null
    lateinit var type: String

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
                val bitmapDrawable = imageView.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                val model = ClassificationModel(requireContext())
                val chkBird: String = model.execution(bitmap, "bird")
                if (chkBird.toInt() == 1) {
                    birdName.text = "새 이름: " + model.execution(bitmap, "specie")
                    birdName.append("\nActivity 추가 구현 필요")
                } else {
                    birdName.text = ""
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
        fun newInstance(path: String?, type: String): ShowImageFragment {
            val fragment = ShowImageFragment()

            val bundle = Bundle()
            bundle.putString("type", type)
            bundle.putString("path", path)
            fragment.arguments = bundle

            return fragment
        }
    }

    fun getFromAlbum() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        activityLauncher.launch(intent)
    }

    @SuppressLint("Recycle")
    fun handleImage(data: Intent?) {
        val uri = data?.data
        Glide.with(requireActivity()).asBitmap().load(uri).into(binding.imageView)
    }

}