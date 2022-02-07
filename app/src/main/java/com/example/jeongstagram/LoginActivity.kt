package com.example.jeongstagram

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null //authentication library 불러옴
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance() //authentication library 불러옴
        signup.setOnClickListener{
            signinAndSignup()
        }
        googlelogin.setOnClickListener{
            //First Step
            googleLogin()
        }
        facebooklogin.setOnClickListener{
            //First Step
            facebookLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //printHashKey() //facebook로그인을 위해 해쉬값 가져오는것
        callbackManager = CallbackManager.Factory.create()
    }
    //LT+mHuFZqP6J0Q/VWdJ9mc+3MJI= 해쉬값 변환

    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)

    }

    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    //Second Step
                    handleFacebookAccessToken(result?.accessToken) //로그인 성공하면 페이스북 데이터를 파이어베이스에 넘김
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }

            })
    }

    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{ //회원가입한 결과값 불러오는 코드
                    task -> //task parameter 생성
                if(task.isSuccessful){
                    //Third step (facebook)
                    //아이디, 패스워드 맞았을떄 로그인됨
                    moveMainPage(task.result?.user)
                }else{
                    //로그인 틀렸을때 나타남
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode,data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if(result!!.isSuccess){//성공한경우 파이어베이스에 넘겨주기
                var account = result?.signInAccount
                //Second step
                firebaseAuthGoogle(account)
            }
        }
    }

    fun firebaseAuthGoogle(account: GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{ //회원가입한 결과값 불러오는 코드
                    task -> //task parameter 생성
                if(task.isSuccessful){
                    //아이디, 패스워드 맞았을떄 로그인됨
                    moveMainPage(task.result?.user)
                }else{
                    //로그인 틀렸을때 나타남
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signinAndSignup(){ //회원가입 코드
        auth?.createUserWithEmailAndPassword(email_edit.text.toString(), pwd_edit.text.toString())
            ?.addOnCompleteListener{ //회원가입한 결과값 불러오는 코드
                    task -> //task parameter 생성
                    if(task.isSuccessful){
                        //아이디 생성되었을 때 필요한 코드들
                        moveMainPage(task.result?.user)
                    }else if(!task.exception?.message.isNullOrEmpty()){
                        //로그인 에러로 에러메시지 출력
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }else{
                        //회원가입 에러메시지 아닌 경우
                        signinEmail()
                    }
            }
    }

    fun signinEmail(){ //로그인 코드
        auth?.signInWithEmailAndPassword(email_edit.text.toString(), pwd_edit.text.toString())
            ?.addOnCompleteListener{ //회원가입한 결과값 불러오는 코드
                    task -> //task parameter 생성
                if(task.isSuccessful){
                    //아이디, 패스워드 맞았을떄 로그인됨
                    moveMainPage(task.result?.user)
                }else{
                    //로그인 틀렸을때 나타남
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }

    }

    fun moveMainPage(user: FirebaseUser?) {//로그인 성공했을떄 다음 페이지로 넘어가는 함수
        if(user!=null){ //파이어 베이스 유저상태 넘겨줌 - 있을 경우
            startActivity(Intent(this, MainActivity::class.java)) //메인화면으로 넘겨줌

        }
    }
}