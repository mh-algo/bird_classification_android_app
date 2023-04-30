package com.earlybird.catchbird.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.earlybird.catchbird.ClassificationModel
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.FragmentModelResultBinding
import com.earlybird.catchbird.encyclopedia.EncyclopediaBirdInforActivity
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
        imageStream?.close()

        val model = ClassificationModel(requireContext())
        model.execution(bitmap, "specie")

        (activity as MainActivity).searchBirdImage()     // model output에 해당하는 새 데이터 검색
        showResultImage(binding)    // recyclerView
    }

    private fun showResultImage(binding: FragmentModelResultBinding) {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        val adapter = ClassificationAdapter()

        binding.recyclerView.adapter = adapter

        adapter.listener = object: OnBirdImageClickListener {
            override fun onItemClick(
                holder: ClassificationAdapter.ViewHolder?,
                view: View?,
                position: Int
            ) {
                val intent = Intent(context, EncyclopediaBirdInforActivity::class.java)
                intent.putExtra("birdKor", BirdImageList.data[position].birdKor)
                if(type=="camera")
                    intent.putExtra("cameraUri", imageUri.toString())
                startActivity(intent)
            }
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