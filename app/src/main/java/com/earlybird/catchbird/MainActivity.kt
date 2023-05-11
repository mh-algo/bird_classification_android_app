package com.earlybird.catchbird

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.earlybird.catchbird.community.CommunityFragment
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.data.BirdInfoData
import com.earlybird.catchbird.databinding.ActivityMainBinding
import com.earlybird.catchbird.encyclopedia.EncyclopediaFragment
import com.earlybird.catchbird.home.CameraFragment
import com.earlybird.catchbird.map.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.storage.FirebaseStorage


class MainActivity : AppCompatActivity(), AutoPermissionsListener {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val TAG = "MainActivity"
    private val databaseName:String = "birdName"
    private var database: SQLiteDatabase? = null
    private val birdImage = "bird_image"
    private val birdInfo = "bird_info"
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    private var currentUserUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth?.currentUser?.uid

        //바텀 네비게이션뷰 안의 아이템 설정
       findViewById<BottomNavigationView>(R.id.nav_view).run { setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, CameraFragment()).commit()
                R.id.navigation_encyclopedia -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, EncyclopediaFragment()).commit()
                R.id.navigation_map -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, MapFragment()).commit()
                R.id.navigation_community -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, CommunityFragment()).commit()
            }
            true
        }
        selectedItemId=R.id.navigation_home
        }

        createDatabase()
        when(checkDatabaseTable()) {
            0 -> {
                Log.d(TAG, "테이블 없음!!!!!!!!!")
                createBirdImageTable()
                insertBirdImageData()
                createBirdInfoTable()
                insertBirdInfoData()
            }
            1 -> Log.d(TAG, "테이블 있음!!!!!!!!!")
        }

        AutoPermissions.Companion.loadAllPermissions(this, 101)
    }

    private fun createDatabase() {
        database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null)
    }

    private fun checkDatabaseTable(): Int {
        if (database == null) {
            Log.e(TAG, "데이터베이스 오픈 안함!!")
            return -1
        }

        // 함수 반환 값이 1이면 bird_image 테이블 존재
        // 0이면 bird_image 테이블 없음
        var cnt: Int = 0
        val sql = "select count(*) as count from sqlite_master where type='table' and name='$birdImage'"
        val cursor = database?.rawQuery(sql, null)
        if (cursor != null) {
            cursor.moveToNext()
            cnt = cursor.getInt(0)
            cursor.close()
        }
        return cnt
    }

    private fun createBirdImageTable() {
        if (database == null) {
            Log.e(TAG, "데이터베이스 오픈 안함!!")
            return
        }
        // 새 이름(한글), 새 이름(영어), 새 이미지(수컷), 새 이미지(암컷), 모델 index를 갖는 테이블 생성
        val sql = "create table if not exists ${birdImage}" +
                "(specie_k text PRIMARY KEY, " +
                " specie_e text, " +
                " image_m text, " +
                " image_f text, " +
                " model_idx text)"

        database?.execSQL(sql)
        Log.d(TAG, "$birdImage 테이블 생성함")
    }

    private fun insertBirdImageData() {
        if (database == null) {
            Log.e(TAG, "데이터베이스 오픈 안함!!")
            return
        }

        val csvData = Utils.readAllCsvData(this, "bird_data.csv")
        for (data in csvData) {
            val dataArray: Array<String> = arrayOf(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4]
            )

            val sql = "insert into ${birdImage} " +
                    "(specie_k," +
                    " specie_e, " +
                    " image_m, " +
                    " image_f, " +
                    " model_idx)" +
                    " values " +
                    "(?, ?, ?, ?, ?)"

            database?.execSQL(sql, dataArray)
        }
        Log.e(TAG, "$birdImage 테이블 데이터 추가 완료!!")
    }

    private fun createBirdInfoTable() {
        if (database == null) {
            Log.e(TAG, "데이터베이스 오픈 안함!!")
            return
        }
        // 새 이름(한글), 도감 정보를 갖는 테이블 생성
        val sql = "create table if not exists ${birdInfo}" +
                "(specie text PRIMARY KEY, " +
                " info text)"

        database?.execSQL(sql)
        Log.d(TAG, "$birdInfo 테이블 생성함")
    }

    private fun insertBirdInfoData() {
        if (database == null) {
            Log.e(TAG, "데이터베이스 오픈 안함!!")
            return
        }

        val csvData = Utils.readAllCsvData(this, "bird_info.csv")
        for (data in csvData) {
            val dataArray: Array<String> = arrayOf(
                data[0],
                data[1]
            )

            val sql = "insert into ${birdInfo} " +
                    "(specie," +
                    " info)" +
                    " values " +
                    "(?, ?)"

            database?.execSQL(sql, dataArray)
        }
        Log.e(TAG, "$birdInfo 테이블 데이터 추가 완료!!")
    }
    fun ChangePage(pageId: Int){
        val move = findViewById<BottomNavigationView>(R.id.nav_view)
        move.selectedItemId=pageId
    }
    fun searchBirdImage() {
        val resultArray = BirdImageList.modelData
        BirdImageList.data.clear()
        for (data in resultArray) {
            // model output에 해당하는 새 검색
            val sql = "select specie_k, image_m from $birdImage where model_idx = ${data.idx}"
            val cursor = database?.rawQuery(sql, null)
            if (cursor != null) {
                cursor.moveToNext()

                val specie_k = cursor.getString(0)  // 새 이름(한글)
                val image_m = cursor.getString(1)   // 새 사진(수컷)

                BirdImageList.data.add(BirdImageData(specie_k, null, image_m, null))
                cursor.close()
            }
        }
    }

    fun loadAllImageData() {
        // DB에 있는 모든 새 이미지 검색
        val sql = "select * from $birdImage"
        val cursor = database?.rawQuery(sql, null)
        if (cursor != null) {
            BirdImageList.data.clear()

            for (i in 0 until cursor.count) {
                cursor.moveToNext()
                val specie_k = cursor.getString(0)
                val specie_e = cursor.getString(1)
                val image_m = cursor.getString(2)
                val image_f = cursor.getString(3)

                BirdImageList.data.add(BirdImageData(specie_k, specie_e, image_m, image_f))
            }
            cursor.close()
        }
    }

    fun searchBirdInfo(specie: String) {
        // DB에 있는 새 이름으로 검색
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AutoPermissions.parsePermissions(this, requestCode, permissions as Array<String>, this)
    }

    override fun onDenied(requestCode: Int, permissions: Array<String>) {
        Log.d(TAG, "거부된 권한 개수 : ${permissions.size}")
    }

    override fun onGranted(requestCode: Int, permissions: Array<String>) {
        Log.d(TAG, "허용된 권한 개수 : ${permissions.size}")
    }
}