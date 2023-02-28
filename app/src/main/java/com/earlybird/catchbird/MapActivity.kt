package com.earlybird.catchbird

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.earlybird.catchbird.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {
    private val binding: ActivityMapBinding by lazy {
        ActivityMapBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}