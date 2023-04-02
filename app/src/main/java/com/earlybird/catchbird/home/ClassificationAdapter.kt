package com.earlybird.catchbird.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.ItemClassificationBinding
import kotlin.math.roundToInt

class ClassificationAdapter: RecyclerView.Adapter<ClassificationAdapter.ViewHolder>() {
    lateinit var listener: OnBirdImageClickListener

    override fun getItemCount() = BirdImageList.data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = ItemClassificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(position)
    }

    inner class ViewHolder(val binding: ItemClassificationBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                listener.onItemClick(this, binding.root, adapterPosition)
            }
        }

        fun setItem(position: Int) {
            val data = BirdImageList.data[position]
            val accuracy = BirdImageList.modelData[position].accuracy?.times(10000)?.let { it.roundToInt() / 100.toFloat() }
            binding.specie.text = data.birdKor
            binding.accuracy.text = "$accuracy%"

            Glide.with(binding.root).load(data.imageMale).centerCrop().into(binding.image)
        }
    }
}