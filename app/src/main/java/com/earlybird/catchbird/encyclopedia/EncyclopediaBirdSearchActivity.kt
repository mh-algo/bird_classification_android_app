package com.earlybird.catchbird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdSearchBinding

class EncyclopediaBirdSearchActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdSearchBinding by lazy {
        ActivityEncyclopediaBirdSearchBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}