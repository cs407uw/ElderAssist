package com.cs407.alderassist_tutorial

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class VideoItem(val title: String, val videoId: String)

class VideoAdapter(
    private val videoList: List<VideoItem>,
    private val onVideoClick: (String) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.videoThumbnail)
        val title: TextView = view.findViewById(R.id.videoTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoItem = videoList[position]
        Log.d("VideoAdapter", "Binding video: ${videoItem.title} at position $position")
        val thumbnailUrl ="https://img.youtube.com/vi/${videoItem.videoId}/hqdefault.jpg"
        Glide.with(holder.thumbnail.context)
            .load(thumbnailUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .into(holder.thumbnail)

        holder.title.text = videoItem.title
        holder.itemView.setOnClickListener {
            onVideoClick(videoItem.videoId)
        }
        Log.d("VideoAdapter", "Binding video: ${videoItem.title}")
    }

    override fun getItemCount(): Int {
        Log.d("VideoAdapter", "Item count: ${videoList.size}")
        return videoList.size
    }

}
