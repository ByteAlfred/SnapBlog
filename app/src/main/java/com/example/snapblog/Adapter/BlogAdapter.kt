package com.example.snapblog.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapblog.BlogDetail
import com.example.snapblog.Models.Blog
import com.example.snapblog.R

class BlogAdapter(
    private val context: Context,
    private val blogList: MutableList<Blog>,
    param: (Any) -> Unit
) :


    // ...
    RecyclerView.Adapter<BlogAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val blogImageView: ImageView = itemView.findViewById(R.id.blogImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog, parent, false)
        return ViewHolder(view)
    }

    private var onItemClick: ((Blog) -> Unit)? = null

    // Set the item click listener
    fun setOnItemClickListener(listener: (Blog) -> Unit) {
        onItemClick = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentBlog = blogList[position]

        // Load image using Glide library with the correct context (holder.itemView.context)
        Glide.with(holder.itemView.context)
            .load(currentBlog.imageUrl)
            .into(holder.blogImageView)

        holder.titleTextView.text = currentBlog.title
        holder.dateTextView.text = currentBlog.date

        // Set up click listener
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BlogDetail::class.java)
            intent.putExtra("blogId", currentBlog.blogId)
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return blogList.size
    }

    fun setData(newBlogList: List<Blog>) {
        blogList.clear()
        blogList.addAll(newBlogList)
        notifyDataSetChanged()
    }





}
