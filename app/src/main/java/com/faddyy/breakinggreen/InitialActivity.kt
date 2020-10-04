package com.faddyy.breakinggreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.faddyy.breakinggreen.augmentedimage.AugmentedImageActivity
import com.faddyy.breakinggreen.city.MainActivity
import kotlinx.android.synthetic.main.activity_initial.*

class InitialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        newCity.setOnTouchListener { _, _ ->
            startActivity(Intent(this, RegionsActivity::class.java) )
            true
        }

        loadOld.setOnTouchListener {_, _ ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.LOAD_SAVED, true)
            startActivity(intent)
            true
        }

        augmented.setOnTouchListener { _, _ ->
            startActivity(Intent(this, AugmentedImageActivity::class.java) )
            true
        }
    }
}
