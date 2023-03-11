package com.earlybird.catchbird.community

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.earlybird.catchbird.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity(){
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    //Firebase Auth 관리 클래스
    var auth: FirebaseAuth? = null

    //구글 로그인 관리 클래스
    //var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
        auth?.createUserWithEmailAndPassword(binding.emailEditText.text.toString(), binding.pwEditText.text.toString())
            ?.addOnCompleteListener { task ->
                //progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.signup_complete), Toast.LENGTH_SHORT).show()
                    moveMainPage(auth?.currentUser)
                }
                else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
                else {
                    signinEmail()
                }
            }
    }

    fun emailLogin() {
        if (emailEditText.text.toString().isNUllOrEmpty() ||
            pwEditText.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.signout_fail_null), Toast.LENGTH_SHORT).show()
        }
        else {
            progressBar.visibility = View.VISIBLE
            createAndLoginEmail()
        }
    }
    */

    override fun onStart(){
        super.onStart()

        //자동 로그인 설정
        moveMainPage(auth?.currentUser)
    }

}