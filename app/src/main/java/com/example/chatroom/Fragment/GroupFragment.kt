package com.example.chatroom.Fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.chatroom.Activity.PrivateActivity
import com.example.chatroom.Adapter.ChatAdapter
import com.example.chatroom.Item.ChatItem
import com.example.chatroom.R
import com.example.chatroom.Utils.FileSaver
import com.example.chatroom.Utils.RealPathUtil
import com.example.chatroom.model.Common
import com.example.chatroom.model.Content
import com.google.gson.GsonBuilder
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.io.File


class GroupFragment : Fragment() {

    private var messageList: ListView? =null
    private var input:EditText?=null
    var msgs= ArrayList<ChatItem>()
    lateinit var adapter: ChatAdapter
    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
    private var RequestCodeGetFile:Int=1
    private var groupID=1
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Common.handler_addMsg->{
                    val message: com.example.chatroom.DataBase.Message =
                        gson.fromJson(msg.obj.toString(), com.example.chatroom.DataBase.Message::class.java)
                    val item=ChatItem(true,message.body,Content.idNameRecord[message.from],message.from)
                    msgs.add(item)
                    adapter.notifyDataSetChanged()
                }
                Common.handler_sendFileSuccess->{
                    Toast.makeText(context,msg.obj.toString(),Toast.LENGTH_SHORT).show()
                }
                Common.handler_fileInfo->{
                    var fileSaver= msg.obj as FileSaver
                    val item=ChatItem(true,fileSaver.fileName,Content.idNameRecord[fileSaver.from],fileSaver.from,fileSaver)
                    msgs.add(item)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Content.groupChatHandler=handler
        messageList=activity?.findViewById<ListView>(R.id.groupListView)
        messageList?.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL)
        messageList?.setStackFromBottom(true)
        input=activity?.findViewById(R.id.groupInput)

        for (i in 0..1){
            val item=ChatItem(true,"欢迎加入聊天室","用户"+i,i)
            msgs.add(item)
        }
        adapter = ChatAdapter(context, R.layout.chat_item, msgs)
        messageList?.adapter=adapter
        messageList?.setOnItemClickListener { parent,view,position,id ->
            var item= msgs?.get(position) as ChatItem
            if(item.fileSaver!=null){
                Toast.makeText(context,item.fileSaver.fileName,Toast.LENGTH_SHORT).show()
                showDonwloadDialog(item.fileSaver)
            }
        }
        activity?.findViewById<Button>(R.id.buttonGroupSend)?.setOnClickListener(){
            val str=input?.text.toString()
            if (!str.equals("")){
                val item=ChatItem(false,str,"自己",0)
                input?.text?.clear()

                Content.client.sendGroupMsg(1,str)
                msgs.add(item)
                adapter.notifyDataSetChanged()
            }
        }
        //发送文件
        activity?.findViewById<ImageButton>(R.id.button_send_file)?.setOnClickListener(){
            checkPermission()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "*/*"
            startActivityForResult(intent, RequestCodeGetFile)
        }
        //监听键盘
        activity?.let {
            setEventListener(it, object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen){
                        messageList?.layoutParams=
                            messageList?.width?.let { it1 -> RelativeLayout.LayoutParams(it1,1650/2) }
                    }else{
                        messageList?.layoutParams=
                            messageList?.width?.let { it1 -> RelativeLayout.LayoutParams(it1,1650) }
                    }
                }
            })
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        //
        if(resultCode==Activity.RESULT_OK){
            when(requestCode){
                RequestCodeGetFile->{
                    //发送文件
                    intentData?.let { data ->
                        var uri=data.data
                        var file=RealPathUtil.getRealPath(context,uri)
                        System.out.println()
                        Content.client.sendFileGroup(file,groupID)

                        val item=ChatItem(false,"发送文件:"+ File(file).name,"自己",Content.id)
                        msgs.add(item)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
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
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) requireList.add(permission)
        }
        if (requireList.isNotEmpty()) {
            activity?.let { ActivityCompat.requestPermissions(it, permissions, 1) }
        }
    }

    //显示删除录音提示框
    private fun showDonwloadDialog(fileSaver: FileSaver) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("文件下载")
        builder.setMessage("文件名:"+fileSaver.fileName+"\n您确定要下载当前文件吗？")

        builder.setPositiveButton(
            "确定"
        ) { _, _ ->;
        }
        builder.setNegativeButton("取消", null)
        val dialog = builder.create()
        dialog.show()
    }


}