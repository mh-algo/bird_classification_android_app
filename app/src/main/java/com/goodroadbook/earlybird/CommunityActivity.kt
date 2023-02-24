package com.goodroadbook.earlybird

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goodroadbook.earlybird.databinding.ActivityCommunityBinding

class CommunityActivity : AppCompatActivity() {
    private val binding: ActivityCommunityBinding by lazy {
        ActivityCommunityBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}