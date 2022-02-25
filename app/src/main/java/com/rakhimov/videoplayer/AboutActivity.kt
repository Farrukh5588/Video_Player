package com.rakhimov.videoplayer

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rakhimov.videoplayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.themesList[MainActivity.themeIndex])
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
        binding.aboutText.text = "Developed By: Rakhimov Farrukh,Rakhimov Khozhiakbar. \n\nIf you want to feedback, I will love to hear that."
    }
}