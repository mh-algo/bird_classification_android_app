package com.earlybird.catchbird

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.databinding.ActivityMainBinding
import com.earlybird.catchbird.encyclopedia.EncyclopediaFragment
import com.earlybird.catchbird.home.HomeFragment
import com.earlybird.catchbird.map.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener


class MainActivity : AppCompatActivity(), AutoPermissionsListener {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        //바텀 네비게이션뷰 안의 아이템 설정
       findViewById<BottomNavigationView>(R.id.nav_view).run { setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, HomeFragment()).commit()
                R.id.navigation_encyclopedia -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, EncyclopediaFragment()).commit()
                R.id.navigation_map -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, MapFragment()).commit()
                R.id.navigation_community -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, HomeFragment()).commit()
            }
            true
        }
        selectedItemId=R.id.navigation_home
        }


        AutoPermissions.Companion.loadAllPermissions(this, 101)
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
        Log.d("Main", "거부된 권한 개수 : ${permissions.size}")
    }

    override fun onGranted(requestCode: Int, permissions: Array<String>) {
        Log.d("Main", "허용된 권한 개수 : ${permissions.size}")
    }
}

