package com.example.chatroom.Client;//package com.example.chatroom.Client;

import android.os.Message;

import com.example.chatroom.model.Common;
import com.example.chatroom.model.Content;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class VoiceChatThread implements Runnable{

    public Socket socket;
    private DataOutputStream output;
    private final int from;
    private final int to;
    public VoiceChatThread(String hostname, int port, int from, int to) {
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
            DataInputStream input= new DataInputStream(socket.getInputStream());
            output.writeInt(from);
            output.writeInt(to);
            Content.voiceOutputStream=output;
            int count=0;
            while(Content.isVoice){
                readAudio(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readAudio(DataInputStream input) throws IOException {
        //收
        byte[] audioBuffer=new byte[4096];
        input.readFully(audioBuffer);
        if (audioBuffer != null) {
            //播放对方发送来的音频
            if(Content.videoChatHandler!=null){
                Message msg=new Message();
                msg.obj=audioBuffer;
                msg.what= Common.handler_voiceData;
                Content.videoChatHandler.sendMessage(msg);
            }
        }
    }

}
