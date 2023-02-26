package com.goodroadbook.earlybird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goodroadbook.earlybird.databinding.ActivityEncyclopediaBinding
import com.goodroadbook.earlybird.databinding.ActivityEncyclopediaRankingBinding

class EncyclopediaRankingActivity : AppCompatActivity() {

    private val binding: ActivityEncyclopediaRankingBinding by lazy {
        ActivityEncyclopediaRankingBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}