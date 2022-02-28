package com.rakhimov.videoplayer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rakhimov.videoplayer.databinding.ActivityMainBinding
import com.rakhimov.videoplayer.databinding.ThemeViewBinding
import java.io.File
import kotlin.Exception
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var runnable: Runnable? = null
    private lateinit var currentFragment: Fragment

    companion object{
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
        lateinit var searchList: ArrayList<Video>
        var search: Boolean = false
        var themeIndex: Int = 0
         var sortValue: Int = 0
        val themesList = arrayOf(R.style.BlueNav, R.style.TealNav, R.style.PurpleNav,
            R.style.GreenNav,R.style.RedNav, R.style.GreyNav)
        var dataChanged: Boolean = false
        var adapterChanged: Boolean = false
        val sortList = arrayOf(MediaStore.Video.Media.DATE_ADDED + " DESC", MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.TITLE + " DESC", MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.SIZE + " DESC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editor = getSharedPreferences("Themes", MODE_PRIVATE)
        themeIndex = editor.getInt("themeIndex", 0)

        setTheme(themesList[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for Now Draw
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()){
            folderList = ArrayList()
            videoList =getAllVideos(this)
            setFragment(VideosFragment())

            runnable = Runnable {
                if (dataChanged){
                    videoList = getAllVideos(this)
                    dataChanged = false
                    adapterChanged = true
                }
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable!!, 0)
        }
       binding.bottomNav.setOnItemSelectedListener {
           if (dataChanged) videoList = getAllVideos(this)
           when(it.itemId){
               R.id.videoView -> setFragment(VideosFragment())
               R.id.folderView -> setFragment(FoldersFragment())
           }
           return@setOnItemSelectedListener true
       }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.themesNav -> {
                    val customDialog = LayoutInflater.from(this).inflate(R.layout.theme_view, binding.root, false)
                    val bindingTV = ThemeViewBinding.bind(customDialog)
                    val dialog = MaterialAlertDialogBuilder(this).setView(customDialog)
                        .setTitle("Select Theme")
                        .create()
                    when(themeIndex){
                        0 -> bindingTV.themeBlue.setBackgroundColor(Color.WHITE)
                        1 -> bindingTV.themeTeal.setBackgroundColor(Color.WHITE)
                        2 -> bindingTV.themePurpule.setBackgroundColor(Color.WHITE)
                        3 -> bindingTV.themeGreen.setBackgroundColor(Color.WHITE)
                        4 -> bindingTV.themeRed.setBackgroundColor(Color.WHITE)
                        5 -> bindingTV.themeGrey.setBackgroundColor(Color.WHITE)
                    }
                    bindingTV.themeBlue.setOnClickListener { saveTheme(0) }
                    bindingTV.themeTeal.setOnClickListener { saveTheme(1) }
                    bindingTV.themePurpule.setOnClickListener { saveTheme(2) }
                    bindingTV.themeGreen.setOnClickListener { saveTheme(3) }
                    bindingTV.themeRed.setOnClickListener { saveTheme(4) }
                    bindingTV.themeGrey.setOnClickListener { saveTheme(5) }
                    dialog.show()
                }
                R.id.sortOrderNav -> {
                    val menuItems = arrayOf("Latest", "Oldest", "Name(A to Z)", "Name(Z to A)",
                        "File Size(Smallest)","File Size(Lagest)")
                    var value = sortValue
                    val dialog = MaterialAlertDialogBuilder(this)
                        .setTitle("Sort By")
                        .setPositiveButton("OK"){_, _->
                            val sortEditor = getSharedPreferences("Sorting", MODE_PRIVATE).edit()
                            sortEditor.putInt("sortValue", value)
                            sortEditor.apply()

                            //for restating app
                            finish()
                            startActivity(intent)
                        }
                        .setSingleChoiceItems(menuItems, sortValue){_, pos ->
                            value = pos
                        }
                        .create()
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED)
                }
                R.id.aboutNav -> startActivity(Intent(this,AboutActivity::class.java))
                R.id.exitNav -> exitProcess(1)
            }
            return@setNavigationItemSelectedListener true
        }
    }
    private fun setFragment(fragment: Fragment){
        currentFragment = fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL,fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
    //for requesting pormition
    private fun requestRuntimePermission(): Boolean{
        if(ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),13)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                folderList = ArrayList()
                videoList = getAllVideos(this)
                setFragment(VideosFragment())
            }
            else
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),13)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val gradientList = arrayOf(R.drawable.blue_gradient, R.drawable.teal_gradient, R.drawable.purple_gradient,
            R.drawable.green_gradient, R.drawable.red_gradient, R.drawable.grey_gradient)

        findViewById<LinearLayout>(R.id.gradientLayout).setBackgroundResource(gradientList[themeIndex])

        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    private fun  saveTheme(index: Int){
        val editor = getSharedPreferences("Themes", MODE_PRIVATE).edit()
        editor.putInt("themeIndex", index)
        editor.apply()

        //for restating app
        finish()
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (currentFragment as VideosFragment).adapter.onResult(requestCode, resultCode)
    }
}