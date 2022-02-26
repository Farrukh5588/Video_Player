package com.rakhimov.videoplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rakhimov.videoplayer.databinding.MoreFeaturesBinding
import com.rakhimov.videoplayer.databinding.RenameFieldBinding
import com.rakhimov.videoplayer.databinding.VideoMoreFeaturesBinding
import com.rakhimov.videoplayer.databinding.VideoViewBinding

class VideoAdapter(private val context: Context, private var videoList: ArrayList<Video>,private val isFolder: Boolean = false) : RecyclerView.Adapter<VideoAdapter.MyHolder>() {
    class MyHolder(binding: VideoViewBinding) :RecyclerView.ViewHolder(binding.root){
        val title = binding.videoName
        val folder = binding.folderName
        val duration = binding.duration
        val image = binding.videoImg
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = videoList[position].title
        holder.folder.text = videoList[position].folderName
        holder.duration.text = DateUtils.formatElapsedTime(videoList[position].duration/1000)
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_video_player).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener {
            when{
                videoList[position].id == PlayerActivity.nowPlayingId -> {
                    sendIntent(pos = position, ref = "NowPlaying")
                }
                isFolder -> {
                    PlayerActivity.pipStatus = 1
                    sendIntent(pos = position, ref = "FolderActivity")
                }
                MainActivity.search -> {
                    PlayerActivity.pipStatus = 2
                    sendIntent(pos = position, ref = "SearchedVideos")
                }
                else -> {
                    PlayerActivity.pipStatus = 3
                    sendIntent(pos = position, ref = "AllVideos")
                }
            }
        }
        holder.root.setOnLongClickListener {
            val customDialog = LayoutInflater.from(context).inflate(R.layout.video_more_features, holder.root, false)
            val bindingMF = VideoMoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
                .create()
            dialog.show()

            bindingMF.renameBtn.setOnClickListener {
                dialog.dismiss()
                val customDialogRF = LayoutInflater.from(context).inflate(R.layout.rename_field, holder.root, false)
                val bindingRF = RenameFieldBinding.bind(customDialogRF)
                val dialogRF = MaterialAlertDialogBuilder(context).setView(customDialogRF)
                    .setCancelable(false)
                    .setPositiveButton("Rename"){self, _->
                        self.dismiss()
                    }
                    .setNegativeButton("Cansel"){self, _->
                        self.dismiss()
                    }
                    .create()
                dialogRF.show()
                bindingRF.renameField.text = SpannableStringBuilder(videoList[position].title)
                dialogRF.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor, ContextCompat.getColor(context,R.color.black))
                )
                dialogRF.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor, ContextCompat.getColor(context,R.color.black))
                )
            }
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
    private fun sendIntent(pos: Int, ref: String){
        PlayerActivity.position = pos
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }
    @SuppressLint("NotifyDataSetChanged")
     fun updateList(searchList: ArrayList<Video>){
        videoList = ArrayList()
        videoList.addAll(searchList)
        notifyDataSetChanged()
    }
}