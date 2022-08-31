package com.example.chatroom.Activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.chatroom.Adapter.ViewPager2Adapter
import com.example.chatroom.Fragment.SettingFragment
import com.example.chatroom.Fragment.PrivateFragment
import com.example.chatroom.Fragment.GroupFragment
import com.example.chatroom.Item.ChatItem
import com.example.chatroom.R
import com.example.chatroom.databinding.ActivityMainBinding
import com.example.chatroom.model.Common
import com.example.chatroom.model.Content

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Common.handler_videoChatRequest->{
                    val from = msg.obj as Int
                    showVideoReplyDialog("视频通话","用户"+Content.idNameRecord[from]+"向你发起了视频通话\n是否接受?",from)
                }
                Common.handler_videoChatAccept->{
                    val intent = Intent(baseContext, VideoChatActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        checkPermission()
        Content.mainHandler=handler
        setContentView(binding.root)
        val adapter = ViewPager2Adapter(this)
        adapter.addFragment(GroupFragment())
        adapter.addFragment(PrivateFragment())
        adapter.addFragment(SettingFragment())
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false
        //绑定ViewPager2，实现三个主页面跳转
        getSupportActionBar()?.hide();
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.radioGroupBottom.check(binding.buttonVoiceRecord.id)
                    }
                    1 -> {
                        binding.radioGroupBottom.check(binding.buttonVoiceList.id)
                    }
                    2 -> {
                        binding.radioGroupBottom.check(binding.buttonSetting.id)

                    }
                }
            }
        })
        //点击按钮时变化UI
        binding.radioGroupBottom.setOnCheckedChangeListener { _, checkID ->
            when (checkID) {
                binding.buttonVoiceRecord.id -> {
                    binding.viewPager.currentItem = 0

                    binding.buttonVoiceRecord.setTextColor(Color.GREEN)
                    binding.buttonVoiceList.setTextColor(Color.BLACK)
                    binding.buttonSetting.setTextColor(Color.BLACK)
                    var drawable =
                        AppCompatResources.getDrawable(baseContext, R.drawable.ic_group_select)
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonVoiceRecord.setCompoundDrawables(null, drawable, null, null)
                    drawable = AppCompatResources.getDrawable(
                        baseContext,
                        R.drawable.ic_private_not_select
                    )
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonVoiceList.setCompoundDrawables(null, drawable, null, null)
                    drawable = AppCompatResources.getDrawable(
                        baseContext,
                        R.drawable.icon_setting_notcheck
                    )
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonSetting.setCompoundDrawables(null, drawable, null, null)
                }
                binding.buttonVoiceList.id -> {
                    binding.viewPager.currentItem = 1

                    binding.buttonVoiceRecord.setTextColor(Color.BLACK)
                    binding.buttonVoiceList.setTextColor(Color.GREEN)
                    binding.buttonSetting.setTextColor(Color.BLACK)

                    var drawable = AppCompatResources.getDrawable(
                        baseContext,
                        R.drawable.ic_group_not_select
                    )
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonVoiceRecord.setCompoundDrawables(null, drawable, null, null)
                    drawable =
                        AppCompatResources.getDrawable(baseContext, R.drawable.ic_private_select)
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonVoiceList.setCompoundDrawables(null, drawable, null, null)
                    drawable = AppCompatResources.getDrawable(
                        baseContext,
                        R.drawable.icon_setting_notcheck
                    )
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonSetting.setCompoundDrawables(null, drawable, null, null)
                }
                binding.buttonSetting.id -> {
                    binding.viewPager.currentItem = 2

                    binding.buttonVoiceRecord.setTextColor(Color.BLACK)
                    binding.buttonVoiceList.setTextColor(Color.BLACK)
                    binding.buttonSetting.setTextColor(Color.GREEN)

                    var drawable = AppCompatResources.getDrawable(
                        baseContext,
                        R.drawable.ic_group_not_select
                    )
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonVoiceRecord.setCompoundDrawables(null, drawable!!, null, null)
                    drawable = AppCompatResources.getDrawable(
                        baseContext,
                        R.drawable.ic_private_not_select
                    )
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonVoiceList.setCompoundDrawables(null, drawable, null, null)
                    drawable = AppCompatResources.getDrawable(baseContext, R.drawable.icon_setting)
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    binding.buttonSetting.setCompoundDrawables(null, drawable, null, null)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Content.client.logout()
        Content.mainHandler=null
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
}