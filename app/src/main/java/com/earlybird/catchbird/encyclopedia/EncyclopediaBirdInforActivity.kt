package com.earlybird.catchbird.encyclopedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.earlybird.catchbird.*
import com.earlybird.catchbird.data.BirdImageList.Companion.data
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdInforBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.item_classification.*


class EncyclopediaBirdInforActivity : AppCompatActivity(),ConfirmDialogInterface {
    private val binding: ActivityEncyclopediaBirdInforBinding by lazy {
        ActivityEncyclopediaBirdInforBinding.inflate(layoutInflater)
    }

    private var birdKor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        birdKor = intent.getStringExtra("birdKor").toString()
        val bird_info = MainActivity().loadBirdInfo(birdKor)
        binding.encyclopediaBtnBirdInforOk.setOnClickListener {
            finish()
        }
        binding.encyclopediaBirdLocation.setOnClickListener {
            val dialog = CustomDialog(this)
            dialog.isCancelable = true
            dialog.show(this.supportFragmentManager,"ConfirmDialog")
        }

        binding.encyclopediaBirdInforText2.text= "fsfsdfsffsfsfsf"
        Glide.with(this).load(bird_info).centerCrop().into(binding.encyclopediaBirdInforImage2)
        // binding.encyclopediaBirdInforImage2.setImageResource()// 새 사진 눌렀을 때 해당 새 사진, 정보 출력

    }


    override fun onYesButtonClick(num: Int, theme: Int) {
        TODO("Not yet implemented")
    }
}