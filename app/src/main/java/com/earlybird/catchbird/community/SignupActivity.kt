package com.earlybird.catchbird.community
import android.content.Intent
import com.earlybird.catchbird.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.earlybird.catchbird.MainActivity
import com.earlybird.catchbird.community.model.FollowDTO
import com.earlybird.catchbird.community.model.ProfileDTO
import com.earlybird.catchbird.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {
    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    //Firebase Auth 관리 클래스
    var auth: FirebaseAuth? = null
    var uid : String? = null
    var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Firebase 로그인 통합 관리하는 객체
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //구글 로그인 버튼 세팅
        //binding.googleSignInButton.setOnClickListener{googleLogin()}

        //이메일 로그인 세팅
        binding.signupBtn.setOnClickListener { signup() }

        setContentView(binding.root)
    }

    //필드가 비어있는지 검사
    fun signup() {
        if (binding.emailEditText.text.toString().isNullOrEmpty() || binding.pwEditText.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.signout_fail_null), Toast.LENGTH_SHORT).show()
        } else {
            progress_bar.visibility = View.VISIBLE
            createEmail()
        }
    }

    override fun onBackPressed() { // 뒤로가기 버튼 누르면 메인 액티비티로 이동함
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // 회원가입 메소드
    fun createEmail() {

        auth?.createUserWithEmailAndPassword(binding.emailEditText.text.toString(), binding.pwEditText.text.toString())
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    //아이디 생성이 성공했을 경우
                    Toast.makeText(this,
                        getString(R.string.signup_complete), Toast.LENGTH_SHORT).show()
                    uid = auth?.currentUser?.uid

                    val followDTO = FollowDTO()

                    followDTO.followerCount = 0
                    followDTO.followers = mutableMapOf<String,Boolean>()
                    followDTO.followingCount = 0
                    followDTO.followings = mutableMapOf<String,Boolean>()
                    firestore?.collection("users")?.document(uid.toString())?.set(followDTO)

                    //TODO("닉네임, 이메일 중복 체크 구현하기")
                    val profileDTO = ProfileDTO(binding.nameEditText.text.toString(), "https://firebasestorage.googleapis.com/v0/b/catchbird-c2e4b.appspot.com/o/default_profile.jpg?alt=media&token=e38a1694-5681-400f-99ac-17255f67e28a")
                    firestore?.collection("profileImages")?.document(uid.toString())?.set(profileDTO)
                    
                    finish()

                } else if (task.exception?.message.isNullOrEmpty()) {
                    //회원가입 에러가 발생했을 경우
                    Toast.makeText(this,
                        task.exception!!.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,
                        "이미 회원가입 되어 있습니다. 로그인 해주세요!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

    }

}