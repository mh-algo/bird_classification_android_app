package com.earlybird.catchbird.community

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.databinding.ActivityEncyclopediaBinding
import com.earlybird.catchbird.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {
    var binding = ActivityLoginBinding.inflate(layoutInflater)

    //Firebase Auth 관리 클래스
    var auth: FirebaseAuth? = null

    //var callbackManager: CallbackManager? = null

    //구글 로그인 관리 클래스
    var googleSignInClient: GoogleSignInClient? = null
    //var callbackManager: CallbackManager? = null

    //구글 로그인
    var GOOGLE_LOGIN_CODE = 9001 //Intent request ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Firebase 로그인 통합 관리하는 객체
        auth = FirebaseAuth.getInstance()

        //구글 로그인 옵션
        var gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()


        //구글 로그인 클래스 만들기
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //callbackManager = CallbackManager.Factory.create()
        //오류 수정 필요

        //구글 로그인 버튼 세팅
        binding.googleSignInButton.setOnClickListener{googleLogin()}
  

        setContentView(binding.root)
    }

    fun googleLogin() {
        //progress_bar.visibility = View.VISIBLE
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    fun onAcitivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글에서 승인된 정보 갖고 오기

        if (requestCode == GOOGLE_LOGIN_CODE ) { //&& resultCode == Activity.Result_OK)
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                //progress_bar.visibility = View.GONE
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                //progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    // 다음 페이지 호출 코드 추가
                    // moveMainPage(auth?.currentUser)
                }
            }
    }

/*
    //로그인 성공 시 토스트 출력 후 메인 엑티비티 화면 로드
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

//이메일 회원가입

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

    override fun onStart() {
        super.onStart()

        //자동 로그인 설정
        moveMainPage(auth?.currentUser)
    }

    fun moveMainPage(user: FirebaseUser?) {
        //User is Signed in
        if (user != null) {
            Toast.makeText(
                this, getString(R.string.signin_complete),
                Toast.LENGTH_SHORT
            ).show()
            //startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}