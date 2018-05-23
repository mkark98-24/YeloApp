@file:Suppress("DEPRECATION")

package com.ddinc.yeloapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_form.*


class FormActivity : AppCompatActivity(), LocationListener {
    override fun onLocationChanged(location: Location?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDiabledDialog()
        }

    }

    val TAG = "FormActivity"
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mLocationManager: LocationManager

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
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        btn_gps.setOnClickListener {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PackageManager.PERMISSION_GRANTED -> {
                    if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showGPSDiabledDialog()
                    } else {
                        getPlace()
                    }
                }
                PackageManager.PERMISSION_DENIED ->
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            44
                    )
            }
        }

        btn_submit.setOnClickListener {

        }
    }

    @SuppressLint("MissingPermission")
    private fun getPlace() {
        tv_address.text = ("Detecting...").toString()
        val mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)
        val placeResult: Task<PlaceLikelihoodBufferResponse> = mPlaceDetectionClient.getCurrentPlace(null)
        placeResult.addOnCompleteListener { task ->
            val likelyPlaces: PlaceLikelihoodBufferResponse = task.result
            var likely = 0f
            var place: String? = null
            for (placeLikelihood in likelyPlaces) {
                Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                        placeLikelihood.place.name,
                        placeLikelihood.likelihood))
                if (placeLikelihood.likelihood > likely) {
                    likely = placeLikelihood.likelihood
                    place = placeLikelihood.place.name.toString()
                }
            }
            tv_address.text = place
            likelyPlaces.release()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                44 -> {
                    if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showGPSDiabledDialog()
                    } else {
                        getPlace()
                    }
                }
            }
        } else {
            AlertDialog.Builder(this)
                    .setMessage("We need this permission to read/write files \n " +
                            "Please grant the permission")
                    .setPositiveButton("GIVE PERMISSION", { dialog, i ->
                        ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                45
                        )
                    })
                    .setNegativeButton("NO THANKS", { dialog, i ->
                        Toast.makeText(this, "Sigh! I tried", Toast.LENGTH_SHORT).show()
                    })
                    .create()
                    .show()
        }
    }

    fun showGPSDiabledDialog() {
        AlertDialog.Builder(this)
                .setTitle("GPS Disabled")
                .setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device")
                .setPositiveButton("Enable GPS", { dialog, i ->
                    val onGPS = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    onGPS.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(onGPS)
                })
                .create()
                .show()
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
