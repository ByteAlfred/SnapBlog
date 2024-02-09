package com.example.snapblog.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapblog.Models.Blog
import com.example.snapblog.R

class ProfileBlogAdapter(private val blogList: List<Blog>) : RecyclerView.Adapter<ProfileBlogAdapter.BlogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blog = blogList[position]
        holder.bind(blog)

        Glide.with(holder.itemView.context)
            .load(blog.imageUrl)
            .into(holder.blogImageView)
    }

    override fun getItemCount(): Int {
        return blogList.size
    }

    class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val contentTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val blogImageView: ImageView = itemView.findViewById(R.id.blogImageView)


        fun bind(blog: Blog) {
            titleTextView.text = blog.title
            contentTextView.text = blog.date
            // Add more bindings as needed
        }
    }
}
