package com.example.chatroom.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.chatroom.R


class SettingFragment : Fragment() {

    private var RequestCodeGetImage:Int=1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onStart() {
        super.onStart()

    }
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var button=activity?.findViewById<Button>(R.id.button_change_background)
        button?.setOnClickListener(){
            checkPermission()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, RequestCodeGetImage)
        }
    }

    @SuppressLint("ResourceType")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        /* Inserts flower into viewModel. */
        if (requestCode == RequestCodeGetImage && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->
                var uri=data.data

                val bitmap = MediaStore.Images.Media.getBitmap(context?.getContentResolver(), uri);
                //val bitmap = BitmapFactory.decodeFile(uri)
                val layout1 = activity?.findViewById(R.id.layout_main) as ConstraintLayout

                val drawable: Drawable = BitmapDrawable(bitmap)
                layout1.background = drawable

            }
        }
    }

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

}