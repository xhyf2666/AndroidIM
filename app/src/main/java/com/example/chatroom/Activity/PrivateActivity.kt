package com.example.chatroom.Activity

import android.Manifest
import android.app.Activity
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
import com.example.chatroom.Utils.FileSaver
import com.example.chatroom.Utils.RealPathUtil
import com.example.chatroom.model.Common
import com.example.chatroom.model.Content
import com.google.gson.GsonBuilder
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.io.File

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
                Common.handler_videoChatRequest->{
                    val from = msg.obj as Int
                    showVideoReplyDialog("视频通话","用户"+Content.idNameRecord[from]+"向你发起了视频通话\n是否接受?",from)
                }
                Common.handler_videoChatAccept->{
                    val intent = Intent(baseContext, VideoChatActivity::class.java)
                    startActivity(intent)
                }
                Common.handler_fileInfo->{
                    var fileSaver= msg.obj as FileSaver
                    val item=ChatItem(true,fileSaver.fileName,Content.idNameRecord[fileSaver.from],fileSaver.from,fileSaver)
                    msgs.add(item)
                    adapter.notifyDataSetChanged()
                }
                Common.handler_fileReceiveSuccess->{
                    var fileSaver= msg.obj as FileSaver
                    Toast.makeText(baseContext,fileSaver.fileName+"接收成功",Toast.LENGTH_SHORT).show()
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

        messageList?.setOnItemClickListener { parent,view,position,id ->
            var item= msgs?.get(position) as ChatItem
            if(item.fileSaver!=null){
                Toast.makeText(this,item.fileSaver.fileName,Toast.LENGTH_SHORT).show()
                showDonwloadDialog(item.fileSaver)
            }
        }

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
        findViewById<ImageButton>(R.id.button_send_file_private)?.setOnClickListener(){
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
    public fun checkPermission() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
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

    private fun showDonwloadDialog(fileSaver: FileSaver) {
        val builder = AlertDialog.Builder(this)
        if(fileSaver.isFinish){
            builder.setTitle("文件下载完成")
            builder.setMessage("文件名:"+fileSaver.fileName+"\n保存路径:\n"+fileSaver.location)

            builder.setPositiveButton(
                "确定"
            ) { _, _ ->
            }
            val dialog = builder.create()
            dialog.show()
        }
        else{
            builder.setTitle("文件下载")
            builder.setMessage("文件名:"+fileSaver.fileName+"\n您确定要下载当前文件吗？")

            builder.setPositiveButton(
                "确定"
            ) { _, _ ->
                Content.client.receiveFilePrivate(fileSaver)
            }
            builder.setNegativeButton("取消", null)
            val dialog = builder.create()
            dialog.show()
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

    private fun showVideoReplyDialog(title:String ,message:String,from:Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            "接受"
        ) { _, _ ->
            Content.client.sendVideoChatReply(from,"ok")
        }
        builder.setNegativeButton(
            "拒绝"
        ) { _, _ ->
            Content.client.sendVideoChatReply(from,"reject")
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDialog(title:String ,message:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("确定", null)
        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        //
        if(resultCode== Activity.RESULT_OK){
            when(requestCode){
                RequestCodeGetFile->{
                    //发送文件
                    intentData?.let { data ->
                        var uri=data.data
                        var file= RealPathUtil.getRealPath(this,uri)
                        System.out.println()
                        Content.client.sendFilePrivate(file,frientID)

                        val item=ChatItem(false,"发送文件:"+ File(file).name,"自己",Content.id)
                        msgs.add(item)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Content.privateChatHandler=null
    }

}