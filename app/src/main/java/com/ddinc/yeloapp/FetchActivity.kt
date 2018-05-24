package com.ddinc.yeloapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.ddinc.yeloapp.adapters.FetchAdapter
import kotlinx.android.synthetic.main.activity_fetch.*

class FetchActivity : AppCompatActivity() {

    lateinit var myAdapter: FetchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch)
        myAdapter = FetchAdapter(this)
        rvModel.layoutManager = LinearLayoutManager(this)
        rvModel.adapter = myAdapter
    }

    override fun onStart() {
        super.onStart()
        myAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        myAdapter.stopListening()
    }
}
