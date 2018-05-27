@file:Suppress("DEPRECATION")

package com.ddinc.yeloapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_form.*
import java.io.IOException


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
    lateinit var myRef: DatabaseReference
    lateinit var picUrl: String
    lateinit var storage: FirebaseStorage

    private val LOG_TAG = "AudioRecordTest"
    private var mFileName: String? = null

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false

    private fun startPlaying() {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(mFileName)
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
    }

    private fun startRecording() {
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder!!.setOutputFile(mFileName)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mRecorder?.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        mRecorder!!.start()
    }

    private fun stopRecording() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
    }

    lateinit var idToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val extras = intent.extras
        if (extras != null) {
            prof_name2.text = extras.getString("p_name")
            prof_mail2.text = extras.getString("p_mail")
            picUrl = extras.getString("p_pic")
            idToken = extras.getString("id_token")
            Glide.with(this).load(Uri.parse(picUrl)).into(prof_pic2)
        }


        // TODO: Start using the Places API.
        val autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.name)
                tv_address.text = place.address
                tv_place.text = place.name
                tv_place.visibility = View.VISIBLE
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
        tv_place.visibility = View.GONE

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

        // Record to the external cache directory for visibility
        mFileName = Environment.getExternalStorageDirectory().absolutePath
        mFileName += "/audiorecordtest.3gp"

        btn_play.visibility = View.GONE
        tv_play.visibility = View.GONE

        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                54)

        btn_record.setOnTouchListener { v, event ->

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                    tv_record.text = ("Recording ...").toString()
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording()
                    tv_record.text = ("Tap and hold to record again").toString()
                    btn_play.visibility = View.VISIBLE
                    tv_play.visibility = View.VISIBLE
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        btn_play.setOnTouchListener { v, event ->

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startPlaying()
                    tv_play.text = ("Playing ...").toString()
                }
                MotionEvent.ACTION_UP -> {
                    stopPlaying()
                    tv_play.text = ("Tap and hold to play again").toString()
                }
            }
            v?.onTouchEvent(event) ?: true
        }


        storage = FirebaseStorage.getInstance()

        btn_submit.setOnClickListener {
            idToken = idToken.replace(".", "_")
            idToken = idToken.replace("#", "_")
            idToken = idToken.replace("$", "_")
            idToken = idToken.replace("[", "_")
            idToken = idToken.replace("]", "_")
            Log.i(TAG, String.format("idtoken : %s", idToken))
            val newObject = Model(idToken, prof_name2.text.toString(), prof_mail2.text.toString(), picUrl, tv_place.text.toString() + " " + tv_address.text.toString())
            myRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://yeloapp-1526849284615.firebaseio.com/")
            myRef.child("models").child(idToken).setValue(newObject)
            Toast.makeText(this, "Uplaod successfull ! Press back and click on Fetch button to fetch data .", Toast.LENGTH_SHORT).show()
        }


    }

    public override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }

        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
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
            var address: String? = null
            var place: String? = null
            for (placeLikelihood in likelyPlaces) {
                Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                        placeLikelihood.place.name,
                        placeLikelihood.likelihood))
                if (placeLikelihood.likelihood > likely) {
                    likely = placeLikelihood.likelihood
                    place = placeLikelihood.place.name.toString()
                    address = placeLikelihood.place.address.toString()
                }
            }
            tv_address.text = address
            tv_place.text = place
            tv_place.visibility = View.VISIBLE
            likelyPlaces.release()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                44 -> {
                    if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showGPSDiabledDialog()
                    } else {
                        getPlace()
                    }
                }

                54 -> {
                    //record
                    permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            64)
                }

                64 -> {

                }
            }
        } else {
            AlertDialog.Builder(this)
                    .setMessage("Please grant the permission")
                    .setPositiveButton("YES", { dialog, i ->
                        when (requestCode) {
                            44 -> ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    44
                            )
                            54 -> ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                    54
                            )
                            64 -> ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    64
                            )
                        }
                    })
                    .setNegativeButton("NO", { dialog, i ->
                        onBackPressed()
                        Toast.makeText(this, "Sigh! I tried", Toast.LENGTH_SHORT).show()
                    })
                    .create()
                    .show()
        }
//        if (!permissionToRecordAccepted) finish()
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
