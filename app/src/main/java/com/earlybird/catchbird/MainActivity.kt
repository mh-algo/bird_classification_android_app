package com.earlybird.catchbird

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.earlybird.catchbird.data.BirdImageData
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.databinding.ActivityMainBinding
import com.earlybird.catchbird.encyclopedia.EncyclopediaFragment
import com.earlybird.catchbird.home.CameraFragment
import com.earlybird.catchbird.map.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener


class MainActivity : AppCompatActivity(), AutoPermissionsListener {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val TAG = "MainActivity"
    private val databaseName:String = "birdName"
    private var database: SQLiteDatabase? = null
    private val birdImage = "bird_image"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
                    .replace(R.id.fragmentContainerView, CameraFragment()).commit()
            }
            true
        }
        selectedItemId=R.id.navigation_home
        }

        createDatabase()
        when(checkDatabaseTable()) {
            0 -> {
                Log.d(TAG, "테이블 없음!!!!!!!!!")
                createTable()
                insertBirdImageData()
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

    private fun createTable() {
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

    fun searchBirdImage(idx: String) {
        // model output에 해당하는 새 검색
        val sql = "select specie_k, image_m from $birdImage where model_idx = $idx"
        val cursor = database?.rawQuery(sql, null)
        if (cursor != null) {
            cursor.moveToNext()
            BirdImageList.data.clear()

            val specie_k = cursor.getString(0)  // 새 이름(한글)
            val image_m = cursor.getString(1)   // 새 사진(수컷)

            BirdImageList.data.add(BirdImageData(specie_k, null, image_m.toUri(), null))
            cursor.close()
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

                BirdImageList.data.add(BirdImageData(specie_k, specie_e, image_m.toUri(), image_f.toUri()))
            }
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

