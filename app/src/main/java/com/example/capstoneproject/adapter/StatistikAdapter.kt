package com.example.capstoneproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneproject.R

class StatistikAdapter(private val context : Context,
                       private val texts : Map<String, Int>)
    : RecyclerView.Adapter<StatistikAdapter.TextViewHolder>(){

    private val keys = texts.keys.toList()
    private val values = texts.values.toList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatistikAdapter.TextViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.statistik_rv_item, parent, false)
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: StatistikAdapter.TextViewHolder,
        position: Int
    ) {
        holder.textView.text = "${keys[position]} : ${values[position]}"
    }

    override fun getItemCount(): Int {
        return texts.size
    }

    class TextViewHolder(val view : View) : RecyclerView.ViewHolder(view){
        val textView : TextView = view.findViewById(R.id.tvStatistikRvItem)
    }
}