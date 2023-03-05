package com.earlybird.catchbird.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.earlybird.catchbird.databinding.ActivityCommunityBinding

class CommunityActivity : AppCompatActivity() {
    private val binding: ActivityCommunityBinding by lazy {
        ActivityCommunityBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}