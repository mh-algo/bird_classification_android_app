package com.earlybird.catchbird.community

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.databinding.ActivityWriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_write.*
import java.text.SimpleDateFormat
import java.util.*

class WriteActivity : AppCompatActivity(){
    private val binding: ActivityWriteBinding by lazy {
        ActivityWriteBinding.inflate(layoutInflater)
    }

    val PICK_IMAGE_FROM_ALBUM = 0

    var photoUri: Uri? = null

    var storage: FirebaseStorage? = null
    var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Firebase 스토리지
        storage = FirebaseStorage.getInstance()
        //Firebase Database
        firestore = FirebaseFirestore.getInstance()
        // Firebase Auth
        auth = FirebaseAuth.getInstance()



        binding.addphotoImage.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        binding.addImageButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                binding.addphotoImage.setImageURI(photoUri)
            }
            else { finish() }
        }
    }

    fun contentUpload(){
        binding.progressBar.visibility = View.VISIBLE

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        /*
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener{*/
        storageRef?.putFile(photoUri!!)?.continueWithTask(){ task: com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask  storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()

            //val uri = taskSnapshot.storage.downloadUrl //.downloadUrl 대체
            //데이터베이스에 바인딩할 위치 생성 및 테이블에 데이터 집합 생성

            val contentDTO = ContentDTO()

            // 이미지 주소
            contentDTO.imageUrl = uri!!.toString()
            // 유저 UID
            contentDTO.uid = auth?.currentUser?.uid
            // 유저 닉네임
            //contentDTO.nickname = firestore?.collection("users")?.document(uid!!).get("nickname")
            // 게시물 설명
            contentDTO.explain = binding.addphotoEditExplain.text.toString()
            // 유저 ID
            contentDTO.userId = auth?.currentUser?.email
            //게시물 업로드 시간
            contentDTO.timestamp = System.currentTimeMillis()

            // 게시물 데이터생성 및 엑티비티 종류
            firestore?.collection("image")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)
            finish()

        }
            ?.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
            }
    }
}