package com.ddinc.yeloapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE)
        if (prefs.getBoolean("firstrun", true)) {
            val mainIntent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
            startActivity(mainIntent)
            finish()
            prefs.edit().putBoolean("firstrun", false).apply()
        }else{
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }
//        val mainIntent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
//        startActivity(mainIntent)
//        finish()

    }
}
