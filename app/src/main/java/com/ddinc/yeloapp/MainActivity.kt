package com.ddinc.yeloapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    val REQ_CODE = 4001
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth
    lateinit var mCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PrintHash.printHashKey(this)

        btn_login.setOnClickListener {
            signIn()
        }

        btn_logout.setOnClickListener {
            signOut()
        }

        prof_section.visibility = View.GONE

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("287907690626-pdiv4i7ic0h6rc1ikjhrperdhbi9t2j1.apps.googleusercontent.com")
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        btn_start.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra("p_name", prof_name.text)
            intent.putExtra("p_mail", prof_mail.text)
            intent.putExtra("p_pic", url.toString())
            intent.putExtra("id_token", idToken)
            startActivity(intent)
        }
        btn_fetch.setOnClickListener {
            val i = Intent(this, FetchActivity::class.java)
            startActivity(i)
        }

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create()
        login_button.setReadPermissions("email", "public_profile")
        login_button.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if (currentUser != null) updateUI(currentUser)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQ_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }

        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data)

        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "linkWithCredential:success")
                val user = task.result.user
                updateUI(user)

            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "linkWithCredential:failure", task.exception)
                Toast.makeText(this, "Google Authentication Failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential: AuthCredential = FacebookAuthProvider.getCredential(token.token)

        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "linkWithCredential:success")
                val user: FirebaseUser = mAuth.currentUser!!
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "linkWithCredential:failure", task.exception)
                Toast.makeText(this@MainActivity, "Facebook Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }


    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    fun signIn() {
        val signInIntent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, REQ_CODE)

    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    updateUI(null)
                }
    }

    //Disconnect App
    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this) {
                    // ...
                }
    }

    lateinit var url: Uri
    lateinit var idToken: String

    private fun updateUI(account: FirebaseUser?) {
        if (account != null) {
            Log.d(TAG, "updateUI")
            btn_login.visibility = View.GONE
            login_button.visibility = View.GONE
            custom_login.visibility = View.GONE
            prof_name.text = account.displayName
            prof_mail.text = account.email
            idToken = account.email.toString()
//            account.getIdToken(true).addOnSuccessListener(OnSuccessListener<GetTokenResult> { result ->
//                idToken = result.token!!
//                Log.d(TAG, "GetTokenResult result = " + idToken)
//            })
            url = account.photoUrl!!
            Glide.with(this).load(account.photoUrl).into(prof_pic)
            prof_section.visibility = View.VISIBLE
        } else {
            custom_login.visibility = View.VISIBLE
            login_button.visibility = View.VISIBLE
            btn_login.visibility = View.VISIBLE
            prof_section.visibility = View.GONE
        }

    }
}
