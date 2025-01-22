package com.example.capstoneproject.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneproject.R
import com.example.capstoneproject.activity.MenulisActivity
import com.example.capstoneproject.fragment.DaftarHanacarakaFragmentDirections

class ListHanacarakaAdapter(private val context: Context, private val images : IntArray, private val text : Array<String>,
    private val isKamus : Boolean) :
    RecyclerView.Adapter<ListHanacarakaAdapter.ImageViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageResource = images[position]
        val textResource = text[position]
        holder.imageView.setImageResource(imageResource)
        holder.textView.text = textResource

        holder.itemView.setOnClickListener {
            if(isKamus){
                val action = DaftarHanacarakaFragmentDirections.actionDaftarHanacarakaFragmentToKamusFragment(position)
                holder.view.findNavController().navigate(action)
            }else{
                val intent = Intent(context, MenulisActivity::class.java)
                intent.putExtra("position", position)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ImageViewHolder(val view : View) : RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.ivItemImage)
        val textView : TextView = view.findViewById(R.id.tvItemImage)
    }

}