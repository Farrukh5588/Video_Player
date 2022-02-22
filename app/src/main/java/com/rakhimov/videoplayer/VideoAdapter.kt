package com.rakhimov.videoplayer

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rakhimov.videoplayer.databinding.VideoViewBinding

class VideoAdapter(private val context: Context, private var videolist: ArrayList<Video>) : RecyclerView.Adapter<VideoAdapter.MyHolder>() {
    class MyHolder(binding: VideoViewBinding) :RecyclerView.ViewHolder(binding.root){
        val title = binding.videoName
        val folder = binding.folderName
        val duration = binding.duration
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = videolist[position].title
        holder.folder.text = videolist[position].folderName
        holder.duration.text = DateUtils.formatElapsedTime(videolist[position].duration/1000)
    }

    override fun getItemCount(): Int {
        return videolist.size
    }
}