package com.example.opiscoursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_main, MainPageFragment.create())
                .commit()
        }
    }
}