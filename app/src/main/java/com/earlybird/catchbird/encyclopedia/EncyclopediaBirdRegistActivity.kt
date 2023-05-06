package com.earlybird.catchbird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdRegistBinding
import com.earlybird.catchbird.databinding.ActivityEncyclopediaRankingBinding

class EncyclopediaBirdRegistActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdRegistBinding by lazy {
        ActivityEncyclopediaBirdRegistBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}