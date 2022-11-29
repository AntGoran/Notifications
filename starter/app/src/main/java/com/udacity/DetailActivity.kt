package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        if(intent?.extras != null){
            fileName_textView.text = intent.getStringExtra("filename")
            status_textView.text = intent.getStringExtra("status ")
        }

        fab.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }

}
