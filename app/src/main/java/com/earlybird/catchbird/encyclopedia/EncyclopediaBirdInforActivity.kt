package com.earlybird.catchbird.encyclopedia

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.earlybird.catchbird.*
import com.earlybird.catchbird.data.BirdInfoData
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdInforBinding


class EncyclopediaBirdInforActivity : AppCompatActivity(),ConfirmDialogInterface {
    private val binding: ActivityEncyclopediaBirdInforBinding by lazy {
        ActivityEncyclopediaBirdInforBinding.inflate(layoutInflater)
    }
    private val databaseName:String = "birdName"
    private var database: SQLiteDatabase? = null
    private val birdImage = "bird_image"
    private val birdInfo = "bird_info"
    private var birdKor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (database == null){
            createDatabase()
        }

        birdKor = intent.getStringExtra("birdKor").toString()
        searchBirdInfo(birdKor)

        val bird_info = BirdInfoData.image_m    // 새 기본 이미지
        BirdInfoData.image_f  // 새(암컷) 이미지 추가 필요

        binding.encyclopediaBtnBirdInforOk.setOnClickListener {
            finish()
        }
        binding.encyclopediaBirdLocation.setOnClickListener {
            val dialog = CustomDialog(this)
            dialog.isCancelable = true
            dialog.show(this.supportFragmentManager,"ConfirmDialog")
        }

        binding.encyclopediaBirdInforText2.text= BirdInfoData.info      // 새 정보
        binding.encyclopediaBirdInforImage2.clipToOutline = true
        Glide.with(this).load(bird_info).into(binding.encyclopediaBirdInforImage2)


    }


    override fun onYesButtonClick(num: Int, theme: Int) {
        TODO("Not yet implemented")
    }

    private fun createDatabase() {
        database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null)
    }

    private fun searchBirdInfo(specie: String) {
        val sql = "select specie, image_m, image_f, info from $birdImage, $birdInfo " +
                "where $birdImage.specie_k = $birdInfo.specie and $birdInfo.specie = '$specie'"
        val cursor = database?.rawQuery(sql, null)
        if (cursor != null) {
            cursor.moveToNext()
            BirdInfoData.specie = cursor.getString(0)
            BirdInfoData.image_m = cursor.getString(1).toUri()
            BirdInfoData.image_f = cursor.getString(2).toUri()
            BirdInfoData.info = cursor.getString(3)
            cursor.close()
        }
    }
}