package com.rvtimetrackerdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rvtimetrackerdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.container.id, MainFragment.newInstance(), MainFragment.TAG)
                .commit()
        }
    }
}