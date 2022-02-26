package com.rakhimov.videoplayer

import android.net.Uri
import java.time.Duration

data class Video(val id: String, var title: String, val duration: Long =0, val folderName: String, val size:String,
                 var path: String, var artUri: Uri)

data class Folder(val id: String, val folderName: String)
