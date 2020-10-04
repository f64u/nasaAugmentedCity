package com.faddyy.breakinggreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.faddyy.breakinggreen.city.MainActivity
import com.google.ar.sceneform.rendering.Color
import kotlinx.android.synthetic.main.activity_regions.*

class RegionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regions)

        coastalBtn.setOnTouchListener {_, _ ->
            startActvityWithColor(0x1E90FF)
            true
        }

        desertBtn.setOnTouchListener { _, _ ->
            startActvityWithColor(0xF0E98C)
            true
        }

        tropicalBtn.setOnTouchListener {_, _ ->
            startActvityWithColor(0x228B22)
            true
        }

        polarBtn.setOnTouchListener { _, _ ->
            startActvityWithColor(0xffffff)
            true
        }

        riverBtn.setOnTouchListener { _, _ ->
            startActvityWithColor(0x008000)
            true
        }
    }

    private fun startActvityWithColor(color: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("color", color)
        startActivity(intent)
    }
}
