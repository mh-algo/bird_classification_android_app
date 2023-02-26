package com.goodroadbook.earlybird.encyclopedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goodroadbook.earlybird.MapActivity
import com.goodroadbook.earlybird.databinding.ActivityEncyclopediaBinding

class EncyclopediaActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBinding by lazy {
        ActivityEncyclopediaBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.encyclopediaBtnRanking.setOnClickListener {
            val intent = Intent(this@EncyclopediaActivity, EncyclopediaRankingActivity::class.java)
            startActivity(intent)
        }
    }
}