package com.goodroadbook.earlybird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goodroadbook.earlybird.databinding.ActivityEncyclopediaBirdSearchBinding
import com.goodroadbook.earlybird.databinding.ActivityEncyclopediaRankingBinding

class EncyclopediaBirdSearchActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdSearchBinding by lazy {
        ActivityEncyclopediaBirdSearchBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}