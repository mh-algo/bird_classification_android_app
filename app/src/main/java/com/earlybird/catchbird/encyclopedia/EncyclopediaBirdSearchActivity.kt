package com.earlybird.catchbird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.earlybird.catchbird.Bird
import com.earlybird.catchbird.R
import com.earlybird.catchbird.SearchBird
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdSearchBinding

class EncyclopediaBirdSearchActivity : AppCompatActivity() {
    private val binding: ActivityEncyclopediaBirdSearchBinding by lazy {
        ActivityEncyclopediaBirdSearchBinding.inflate(layoutInflater)
    }
    private var dummy = ArrayList<SearchBird>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        dummy.apply {
            for(i in 0..20){
                add(SearchBird("참새 $i",R.drawable.dummy_bird))
            }
        }
    }
}