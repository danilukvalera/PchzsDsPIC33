package com.daniluk.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.daniluk.R
import kotlinx.android.synthetic.main.activity_extended_data.*

class ExtendedDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extended_data)

        tvDataExtended.text = intent.getStringExtra("log")

    }
}