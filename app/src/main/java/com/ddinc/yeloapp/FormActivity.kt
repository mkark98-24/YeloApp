package com.ddinc.yeloapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_form.*


class FormActivity : AppCompatActivity() {

    val TAG = "FormActivity"
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val extras = intent.extras
        if (extras != null) {
            prof_name2.text = extras.getString("p_name")
            prof_mail2.text = extras.getString("p_mail")
            Glide.with(this).load(Uri.parse(extras.getString("p_pic"))).into(prof_pic2)
        }


        // TODO: Start using the Places API.
        val autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.name)
                tv_address.text = place.address
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Toast.makeText(this@FormActivity, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "An error occurred: $status")

            }
        })


        btn_logout2.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this) {
                        val i = Intent(applicationContext, MainActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(i)
                        finish()
                    }
        }

        btn_gps.setOnClickListener {

        }

        btn_submit.setOnClickListener {

        }
    }

    override fun onStart() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("287907690626-pdiv4i7ic0h6rc1ikjhrperdhbi9t2j1.apps.googleusercontent.com")
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        super.onStart()
    }

}
