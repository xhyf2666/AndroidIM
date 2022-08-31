package com.example.chatroom.Activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatroom.Adapter.ChatAdapter
import com.example.chatroom.Item.ChatItem
import com.example.chatroom.R
import com.example.chatroom.model.Common
import com.example.chatroom.model.Content
import com.google.gson.GsonBuilder
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class PrivateActivity : AppCompatActivity() {


    private var messageList: ListView? =null
    private var input: EditText?=null
    var msgs= ArrayList<ChatItem>()
    lateinit var adapter: ChatAdapter
    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
    private var RequestCodeGetFile:Int=1
    private var frientID=-1
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Common.handler_addMsg->{
                    val message: com.example.chatroom.DataBase.Message =
                        gson.fromJson(msg.obj.toString(), com.example.chatroom.DataBase.Message::class.java)
                    val item=ChatItem(true,message.body,"",message.from)
                    msgs.add(item)
                    adapter.notifyDataSetChanged()
                }
                Common.handler_videoChatReject->{
                    showDialog("视频通话",msg.obj.toString())
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private)
        frientID=intent.getIntExtra("id",-1)
        getSupportActionBar()?.title=Content.idNameRecord[frientID]
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        Content.privateChatHandler=handler
        messageList=findViewById<ListView>(R.id.privateChatListView)
        messageList?.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL)
        messageList?.setStackFromBottom(true)
        input=findViewById(R.id.privateInput)

        adapter = ChatAdapter(this, R.layout.chat_item, msgs)
        messageList?.adapter=adapter
        findViewById<Button>(R.id.buttonPrivateSend)?.setOnClickListener(){
            val str=input?.text.toString()
            if (!str.equals("")){
                val item= ChatItem(false,str,"",Content.id)
                input?.text?.clear()
                Content.client.sendPrivateMsg(frientID,str)
                msgs.add(item)
                adapter.notifyDataSetChanged()
            }
        }
        //发送文件
        findViewById<ImageButton>(R.id.button_send_file)?.setOnClickListener(){
            checkPermission()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "*/*"
            startActivityForResult(intent, RequestCodeGetFile)
        }

        //发送视频请求
        findViewById<ImageButton>(R.id.button_video_chat)?.setOnClickListener(){
            checkPermission()
            Content.client.startVideoChat(frientID)
            Toast.makeText(this,"等待对方回应",Toast.LENGTH_SHORT).show()
            val item=ChatItem(false,"发起视频通话","",Content.id)
            msgs.add(item)
            adapter.notifyDataSetChanged()
        }
        //监听键盘
        let {
            KeyboardVisibilityEvent.setEventListener(it, object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen) {
                        messageList?.layoutParams =
                            messageList?.width?.let { it1 ->
                                RelativeLayout.LayoutParams(
                                    it1,
                                    1650 / 2
                                )
                            }
                    } else {
                        messageList?.layoutParams =
                            messageList?.width?.let { it1 ->
                                RelativeLayout.LayoutParams(
                                    it1,
                                    1650
                                )
                            }
                    }
                }
            })
        }
    }

    //检查权限，获取权限
    private fun checkPermission() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val requireList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) requireList.add(permission)
        }
        if (requireList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog(title:String ,message:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("确定", null)
        val dialog = builder.create()
        dialog.show()
    }

}