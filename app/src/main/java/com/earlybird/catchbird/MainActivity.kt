package com.earlybird.catchbird

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.databinding.ActivityMainBinding
import com.earlybird.catchbird.encyclopedia.EncyclopediaActivity
import com.earlybird.catchbird.encyclopedia.EncyclopediaFragment
import com.earlybird.catchbird.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener


class MainActivity : AppCompatActivity() {
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
                    .replace(R.id.fragmentContainerView, HomeFragment()).commit()
                R.id.navigation_community -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, HomeFragment()).commit()
            }
            true
        }
        selectedItemId=R.id.navigation_home
        }

    }
}

