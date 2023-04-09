package com.earlybird.catchbird.community

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.earlybird.catchbird.R
import com.earlybird.catchbird.community.model.ContentDTO
import com.earlybird.catchbird.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    //Firebase Auth 관리 클래스
    var auth: FirebaseAuth? = null
    var uid : String? = null
    var firestore: FirebaseFirestore? = null

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
        firestore = FirebaseFirestore.getInstance()

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

        //이메일 로그인 세팅
        binding.loginButton.setOnClickListener { emailLogin() }

        setContentView(binding.root)
    }

    fun googleLogin() {
        binding.progressBar.visibility = View.VISIBLE
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Toast.makeText(this,
            getString(R.string.signin_complete), Toast.LENGTH_LONG).show()

        // 구글에서 승인된 정보 갖고 오기
        if (requestCode == GOOGLE_LOGIN_CODE && resultCode == Activity.RESULT_OK) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result!!.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->

                Toast.makeText(this,
                    getString(R.string.signin_complete), Toast.LENGTH_LONG).show()
                if (task.isSuccessful) {

                    binding.progressBar.visibility = View.GONE
                    // 다음 페이지 호출 코드 추가
                    moveMainPage(auth?.currentUser)
                }
            }
    }


    //이메일 회원가입 및 로그인 메소드
    fun createAndLoginEmail() {

        auth?.createUserWithEmailAndPassword(binding.emailEditText.text.toString(), binding.pwEditText.text.toString())
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    //아이디 생성이 성공했을 경우
                    Toast.makeText(this,
                        getString(R.string.signup_complete), Toast.LENGTH_SHORT).show()
                    uid = auth?.currentUser?.uid
                    firestore?.collection("users")?.document()?.set(uid.toString())

                    //다음페이지 호출
                    moveMainPage(auth?.currentUser)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    //회원가입 에러가 발생했을 경우
                    Toast.makeText(this,
                        task.exception!!.message, Toast.LENGTH_SHORT).show()
                } else {
                    //아이디 생성도 안되고 에러도 발생되지 않았을 경우 로그인
                    signinEmail()
                }
            }

    }

    fun emailLogin() {

        if (binding.emailEditText.text.toString().isNullOrEmpty() || binding.pwEditText.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.signout_fail_null), Toast.LENGTH_SHORT).show()

        } else {

            progress_bar.visibility = View.VISIBLE
            createAndLoginEmail()

        }
    }

    //로그인 메소드
    fun signinEmail() {

        auth?.signInWithEmailAndPassword(binding.emailEditText.text.toString(), binding.pwEditText.text.toString())
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE

                if (task.isSuccessful) {
                    //로그인 성공 및 다음페이지 호출
                    moveMainPage(auth?.currentUser)
                } else {
                    //로그인 실패
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }

    }

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
                Toast.LENGTH_LONG
            ).show()
            //startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}