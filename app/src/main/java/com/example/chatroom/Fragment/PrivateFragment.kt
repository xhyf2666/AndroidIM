package com.example.chatroom.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatroom.Activity.PrivateActivity
import com.example.chatroom.Adapter.ChatAdapter
import com.example.chatroom.Adapter.FriendListAdapter
import com.example.chatroom.Item.ChatItem
import com.example.chatroom.R
import com.example.chatroom.model.Common
import com.example.chatroom.model.Content
import java.io.File
import java.util.ArrayList

class PrivateFragment : Fragment() {
    private var path: String = ""
    private var userList: ListView? =null
    var IDs= ArrayList<Int>()
    lateinit var adapter:FriendListAdapter
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Common.handler_updateUserList->{
                    IDs.clear()
                    for ((k,v) in Content.userList){
                        IDs.add(v)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_private, container, false)
    }

    override fun onStart() {
        super.onStart()
        Content.privateHandler=handler
        Thread(Runnable {
            Content.client.getUserNameList()
            Content.client.getUserList()
        }).start()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userList=activity?.findViewById(R.id.privateListView)
        userList?.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL)
        userList?.setStackFromBottom(true)
        userList?.setOnItemClickListener { parent,view,position,id ->
            val id=IDs[position]
            Toast.makeText(context,""+Content.idNameRecord[id],Toast.LENGTH_SHORT).show()
            val intent = Intent(context, PrivateActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        for ((k,v) in Content.userList){
            IDs.add(v)
        }
        adapter = FriendListAdapter(context, R.layout.friend_item, IDs)
        userList?.adapter=adapter
    }

    override fun onResume() {
        super.onResume()
        Thread(Runnable {
            Content.client.getUserList()
            Content.client.getUserNameList()
        }).start()
    }

}