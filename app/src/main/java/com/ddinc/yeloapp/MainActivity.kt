package com.ddinc.yeloapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    val REQ_CODE = 4001
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_login.setOnClickListener {
            signIn()
        }

        btn_logout.setOnClickListener {
            signOut()
        }

        prof_section.visibility = View.GONE

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


    }

    override fun onStart() {
        super.onStart()
        //Check for existing login
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) updateUI(account)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == REQ_CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    fun signIn() {
        val signInIntent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, REQ_CODE)

    }

    fun signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    updateUI(null)
                }

    }

    fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            btn_login.visibility = View.GONE
            prof_name.text = account.displayName
            prof_mail.text = account.email
            Glide.with(this).load(account.photoUrl).into(prof_pic)
            prof_section.visibility = View.VISIBLE
        } else {
            btn_login.visibility = View.VISIBLE
            prof_section.visibility = View.GONE
        }

    }
}
