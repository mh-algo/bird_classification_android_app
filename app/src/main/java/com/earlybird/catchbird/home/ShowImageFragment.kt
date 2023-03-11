package com.earlybird.catchbird.home

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.earlybird.catchbird.ClassificationModel
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.FragmentShowImageBinding

class ShowImageFragment : Fragment() {
    private val binding: FragmentShowImageBinding by lazy {
        FragmentShowImageBinding.inflate(layoutInflater)
    }

    var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bundle = arguments
        val path = bundle?.getString("path")
        uri = Uri.parse(path)

        Glide.with(requireActivity()).asBitmap().load(uri).into(binding.imageView)

        with(binding) {
            againBtn.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, CameraFragment()).commit()
            }
            modelBtn.setOnClickListener {
                val bitmapDrawable = imageView.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                val model = ClassificationModel(requireContext())
                birdName.text = "새 이름: " + model.execution(bitmap)
                birdName.append("\nActivity 추가 구현 필요")
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance(path: String): ShowImageFragment {
            val fragment = ShowImageFragment()

            val bundle = Bundle()
            bundle.putString("path", path)
            fragment.arguments = bundle

            return fragment
        }
    }


}