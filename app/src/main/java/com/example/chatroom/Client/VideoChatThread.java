package com.example.chatroom.Client;//package com.example.chatroom.Client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Message;
import android.util.Log;

import com.example.chatroom.model.Common;
import com.example.chatroom.model.Content;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

public class VideoChatThread implements Runnable{

    public Socket socket;
    private DataOutputStream output;
    private final int from;
    private final int to;

    public VideoChatThread(String hostname, int port, int from, int to) {
        try {
            this.socket = new Socket(hostname,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {

        try {

            output=new DataOutputStream(socket.getOutputStream());
            Content.videoOutputStream=output;
            DataInputStream input= new DataInputStream(socket.getInputStream());
            output.writeInt(from);
            output.writeInt(to);
            int count=0;

            while(Content.isVideo){
                Bitmap img=readImage(input);
                if(img==null)
                    continue;
                if (Content.videoChatHandler!=null){
                    Log.d("videoImg","收到了一张图片");
                    Message msg=new Message();
//                    Matrix matrix = new Matrix();
//
//                    matrix.postRotate(90);
//
//                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(img, img.getWidth(), img.getHeight(), true);
//
//                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    msg.obj=img;
                    msg.what= Common.handler_videoImgUpdate;
                    Content.videoChatHandler.sendMessage(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap readImage(DataInputStream input){
        byte[] sizeAr = new byte[4];
        try {
            input.readFully(sizeAr);
            int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

            byte[] imageAr = new byte[size];
            input.readFully(imageAr);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageAr, 0, imageAr.length);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

