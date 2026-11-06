package com.example.snowflakes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var snowflakes: Snowflakes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        snowflakes = Snowflakes(this)
        setContentView(snowflakes)
    }

    override fun onResume() {
        super.onResume()
        snowflakes.start()
    }

    override fun onPause() {
        snowflakes.stop()
        super.onPause()
    }
}
