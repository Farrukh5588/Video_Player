package com.rakhimov.videoplayer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.rakhimov.videoplayer.databinding.MoreFeaturesBinding
import com.rakhimov.videoplayer.databinding.ThemeViewBinding
import java.io.File
import kotlin.Exception
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    companion object{
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
        lateinit var searchList: ArrayList<Video>
        var search: Boolean = false
        var themeIndex: Int = 0
        val themesList = arrayOf(R.style.BlueNav, R.style.TealNav, R.style.PurpleNav,
            R.style.GreenNav,R.style.RedNav, R.style.GreyNav)
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
            videoList =getAllVideos()
            setFragment(VideosFragment())
        }
       binding.bottomNav.setOnItemSelectedListener {
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
                    dialog.show()
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
                }
                R.id.sortOrderNav -> {
                    val menuItems = arrayOf("Latest", "Oldest", "Name(A to Z)", "Name(Z to A)",
                        "File Size(Smallest)","File Size(Lagest)")
                    val dialog = MaterialAlertDialogBuilder(this)
                        .setTitle("Sort By")
                        .setPositiveButton("OK"){self, _->
                            self.dismiss()
                        }
                        .setSingleChoiceItems(menuItems, 0){_, pos ->
                            Toast.makeText(this@MainActivity, menuItems[pos], Toast.LENGTH_SHORT).show()
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
                videoList = getAllVideos()
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

    @SuppressLint("InlinedApi", "Recycle", "Range")
    private fun getAllVideos(): ArrayList<Video>{
        val templist = ArrayList<Video>()
        val tempFolderList =ArrayList<String>()
        val projection = arrayOf(MediaStore.Video.Media.TITLE,MediaStore.Video.Media.SIZE,MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media.DATA,MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION, MediaStore.Video.Media.BUCKET_ID)
        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null,
        MediaStore.Video.Media.DATE_ADDED + " DESC")
        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()

                    try{
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = Video(title = titleC, id = idC, folderName = folderC, duration = durationC,size = sizeC,
                        path = pathC,artUri = artUriC)
                        if (file.exists()) templist.add(video)

                        //for adding folders
                        if (!tempFolderList.contains(folderC)){
                            tempFolderList.add(folderC)
                            folderList.add(Folder(id = folderIdC, folderName = folderC))
                        }


                    }catch (e:Exception){}
                }while (cursor.moveToNext())
                cursor?.close()
        return templist
    }

    private fun  saveTheme(index: Int){
        val editor = getSharedPreferences("Themes", MODE_PRIVATE).edit()
        editor.putInt("themeIndex", index)
        editor.apply()

        //for restating app
        finish()
        startActivity(intent)
    }
}