package com.earlybird.catchbird.encyclopedia

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.earlybird.catchbird.*
import com.earlybird.catchbird.community.LoginActivity
import com.earlybird.catchbird.community.SignupActivity
import com.earlybird.catchbird.data.BirdInfoData
import com.earlybird.catchbird.data.CaptureTime
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBirdInforBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storageMetadata


class EncyclopediaBirdInforActivity : AppCompatActivity(),ConfirmDialogInterface {
    private val binding: ActivityEncyclopediaBirdInforBinding by lazy {
        ActivityEncyclopediaBirdInforBinding.inflate(layoutInflater)
    }
    private val databaseName:String = "birdName"
    private var database: SQLiteDatabase? = null
    private val birdImage = "bird_image"
    private val birdInfo = "bird_info"
    private var birdKor = ""

    private var latitude: String? = null
    private var longitude: String? = null

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
        binding.encyclopediaBirdInforNameText.text = BirdInfoData.specie // 새 이름
        binding.encyclopediaBirdInforText2.text= BirdInfoData.info      // 새 정보
        binding.encyclopediaBirdInforImage2.clipToOutline = true
        Glide.with(this).load(bird_info).into(binding.encyclopediaBirdInforImage2)

        // ModelResultFragment에서 넘어온 경우

        intent.getStringExtra("cameraUri")?.run {
            binding.imageUpload.visibility = View.VISIBLE
            latitude = intent.getStringExtra("latitude")
            longitude = intent.getStringExtra("longitude")

            binding.imageUpload.setOnClickListener {
                val user = Firebase.auth.currentUser
                if (user==null) {
                    AlertDialog.Builder(this@EncyclopediaBirdInforActivity)
                        .setTitle("로그인 필요")
                        .setMessage("커뮤니티를 이용하시려면 로그인이 필요합니다!")
                        .setPositiveButton("로그인") { dialog, which ->
                            val intent = Intent(this@EncyclopediaBirdInforActivity, LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .setNegativeButton("취소") { dialog, which -> }
                        .setNeutralButton("회원가입") { dialog, which ->
                            val intent = Intent(this@EncyclopediaBirdInforActivity, SignupActivity::class.java)
                            startActivity(intent)
                        }
                        .setCancelable(false) // 뒤로가기 사용불가
                        .create()
                        .show()
                } else {
                    val imageName = this.split('/').last()
                    val imageRef = FirebaseStorage.getInstance().reference.child("userBirdImages/${user.uid}/$birdKor/$imageName")

                    val metadata = storageMetadata {
                        setCustomMetadata("date", CaptureTime.date)
                        setCustomMetadata("time", CaptureTime.time)
                        setCustomMetadata("latitude", latitude)
                        setCustomMetadata("longitude", longitude)
                    }

                    val uploadTask: UploadTask = imageRef.putFile(this.toUri(), metadata)
                    uploadTask.addOnFailureListener {

                    }.addOnSuccessListener {
                        Toast.makeText(this@EncyclopediaBirdInforActivity, "업로드가 완료되었습니다!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


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