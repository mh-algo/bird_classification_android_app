package com.earlybird.catchbird.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.earlybird.catchbird.databinding.ActivityMainBinding
import com.earlybird.catchbird.encyclopedia.EncyclopediaFragment
import com.earlybird.catchbird.home.HomeFragment
import com.earlybird.catchbird.map.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pedro.library.AutoPermissions
import com.pedro.library.AutoPermissionsListener
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity(){
    //Firebase Auth 관리 클래스
    var auth: FirebaseAuth? = null

    //구글 로그인 관리 클래스
    //var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Firebase 로그인 통합 관리하는 객체
        auth = FirebaseAuth.getInstance()



    }
    //로그인 성공 시 토스트 출력 후 메인 엑티비티 화면 로드
    fun moveMainPage(user: FirebaseUser?){
        if (user != null) {
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    //이메일 회원가입
    /*
    fun createAndLoginEmail() {
        auth?.createUserWithEmailAndPassword(emailEditText.text.toString(), pwEditText.text.toString())
            ?.addOnCompleteListener { task ->
                //progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.signup_complete), Toast.LENGTH_SHORT).show()
                }
            }
    } */

}