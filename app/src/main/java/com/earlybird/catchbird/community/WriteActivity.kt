package com.earlybird.catchbird.community

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.databinding.ActivityWritePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_write_post.*
import java.text.SimpleDateFormat
import java.util.*

class WriteActivity : AppCompatActivity(){
    var binding = ActivityWritePostBinding.inflate(layoutInflater)

    val PICK_IMAGE_FROM_ALBUM = 0

    var photoUri: Uri? = null

    var storage: FirebaseStorage? = null
    var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)

        //Firebase 스토리지
        storage = FirebaseStorage.getInstance()
        //Firebase Database
        firestore = FirebaseFirestore.getInstance()
        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"

        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        binding.addphotoImage.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == PICK_IMAGE_FROM_ALBUM) {
                println(data?.data)
                photoUri = data?.data
                binding.addphotoImage.setImageURI(data?.data)
            }
            else { finish() }
        }
    }

    fun contentUpload(){
        binding.progressBar.visibility = View.VISIBLE

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener{
            taskSnapshot -> binding.progressBar.visibility = View.GONE
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()

            val uri = taskSnapshot.uploadSessionUri //.downloadUrl 대체
            //데이터베이스에 바인딩할 위치 생성 및 테이블에 데이터 집합 생성

            val contentDTO = ContentDTO()

            // 이미지 주소
            contentDTO.imageUrl = uri!!.toString()
            // 유저 UID
            contentDTO.uid = auth?.currentUser?.uid
            // 게시물 설명
            contentDTO.explain = binding.addphotoEditExplain.text.toString()
            // 유저 ID
            contentDTO.userId = auth?.currentUser?.email
            //게시물 업로드 시간
            contentDTO.timestamp = System.currentTimeMillis()

            // 게시물 데이터생성 및 엑티비티 종류
            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(RESULT_OK)
            finish()

        }
            ?.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            }
    }
}