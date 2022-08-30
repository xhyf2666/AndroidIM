package com.example.chatroom.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.chatroom.Item.ChatItem
import com.example.chatroom.R
import com.example.chatroom.model.Content

class FriendListAdapter(context: Context?, private val resourceId: Int, objects: List<Int>?) :
    ArrayAdapter<Int?>(context!!, resourceId, objects!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val data = getItem(position)
        val view = LayoutInflater.from(context).inflate(resourceId, parent, false)
        val name = view.findViewById<TextView>(R.id.item_friend_name)
        name.text = Content.idNameRecord[data]
        return view
    }

}