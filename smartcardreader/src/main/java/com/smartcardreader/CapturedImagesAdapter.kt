package com.smartcardreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.li_image.view.*

class CapturedImagesAdapter(var list: ArrayList<String>,var preview : (String)->Unit,var delete : (String)->Unit) :
    RecyclerView.Adapter<CapturedImagesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_image, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(holder.itemView.context)
            .load(list[position])
            .into(holder.itemView.capturedImage)

        holder.itemView.remove.setOnClickListener { delete.invoke(list[position]) }
        holder.itemView.rootView.setOnClickListener { preview.invoke(list[position]) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


}