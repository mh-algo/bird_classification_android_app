package com.earlybird.catchbird.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.earlybird.catchbird.ClassificationModel
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.FragmentModelResultBinding
import java.io.FileNotFoundException
import java.io.InputStream

class ModelResultFragment : Fragment() {
    private val binding: FragmentModelResultBinding by lazy {
        FragmentModelResultBinding.inflate(layoutInflater)
    }

    private var imageUri: Uri? = null
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        imageUri = bundle?.getString("cameraUri")?.toUri()
        type = bundle?.getString("type")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Glide.with(requireActivity()).asBitmap().load(imageUri).into(binding.imageView)
        binding.backBtn.setOnClickListener {
            showImageFragment(imageUri, type)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var imageStream: InputStream? = null
        try {
            imageStream = requireContext().contentResolver.openInputStream(imageUri!!)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)


        val model = ClassificationModel(requireContext())
        model.execution(bitmap, "specie")

        val mainActivity = activity as MainActivity

        mainActivity.searchBirdImage()     // model output에 해당하는 새 데이터 검색

        val resultArray = BirdImageList.data
        for ((idx, data) in resultArray.withIndex()) {
            val percent = BirdImageList.modelData[idx].percent
            binding.textView.append("${data.birdKor}, ${data.imageMale}, $percent\n\n")
        }

    }

    companion object {
        fun newInstance(imageUri: String?, type: String?): ModelResultFragment {
            val fragment = ModelResultFragment()
            val bundle = Bundle()
            bundle.putString("cameraUri", imageUri)
            bundle.putString("type", type)
            fragment.arguments = bundle

            return fragment
        }
    }

    private fun showImageFragment(imageUri: Uri?, type: String?) {
        val fragment = ShowImageFragment.newInstance(imageUri.toString(), type)
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment).commit()
    }
}