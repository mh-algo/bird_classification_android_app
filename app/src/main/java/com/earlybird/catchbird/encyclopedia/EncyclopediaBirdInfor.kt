package com.earlybird.catchbird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdInforBinding
import com.earlybird.catchbird.databinding.ActivityEncyclopediaOtherRankingPageBinding

class EncyclopediaBirdInfor : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdInforBinding by lazy {
        ActivityEncyclopediaBirdInforBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}