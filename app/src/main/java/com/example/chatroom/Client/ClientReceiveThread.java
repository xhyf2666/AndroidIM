package com.example.chatroom.Client;

import com.example.chatroom.DataBase.Message;
import com.example.chatroom.Utils.FileSaver;
import com.example.chatroom.model.Common;
import com.google.gson.Gson;
import com.example.chatroom.model.Content;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ClientReceiveThread implements Runnable {

    private final Client client;
    public Socket socket;
    private final String hostName;
    private final Gson gson =  new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private DataOutputStream output;
    android.os.Message msg;

    public ClientReceiveThread(String hostName,Client client, Socket socket) {
        this.hostName=hostName;
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());

            while (true) {
                int length=input.readInt();
                byte[] buffer=new byte[length];
                input.readFully(buffer);
                if(buffer[0]!=123){
                    handle(buffer);
                    continue;
                }
                String str=new String(buffer,0,length, StandardCharsets.UTF_8);
                Message message = gson.fromJson(str, Message.class);
                String body = message.getBody();
                SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
                String date=dateFormat.format(message.getTime());
                switch (message.getType()) {
                    case SUCCESS:
                        client.setId(message.getTo());
                        Content.id=message.getTo();
                        System.out.println(date+" "+"System:"+body);
                        msg=new android.os.Message();
                        msg.obj="jump to mainActivity";
                        msg.what= Common.handler_login;
                        //跳转到activity
                        Content.loginHandler.sendMessage(msg);
                        break;
                    case GROUP_MSG:
                        System.out.println(date+" "+message.getFrom()+":"+body);
                        if (Content.groupChatHandler!=null){
                            msg=new android.os.Message();
                            msg.obj=str;
                            msg.what= Common.handler_addMsg;
                            Content.groupChatHandler.sendMessage(msg);
                        }
                        break;
                    case PRIVATE_MSG:
                        System.out.println(date+" "+message.getFrom()+"(私):"+body);
                        if (Content.privateChatHandler!=null){
                            msg=new android.os.Message();
                            msg.obj=str;
                            msg.what= Common.handler_addMsg;
                            Content.privateChatHandler.sendMessage(msg);
                        }
                        if(Content.privateChatRecord.containsKey(message.getFrom())){
                            Content.privateChatRecord.get(message.getFrom()).add("0" + body);
                        }else{
                            ArrayList<String> newRecord = new ArrayList<>();
                            newRecord.add("1" + body);
                            Content.privateChatRecord.put(message.getFrom(), newRecord);
                        }
                        break;
                    case FAIL:
                        System.out.println(date+" "+"System:"+body);
                        Content.userName="";
                        break;
                    case USER_LIST:
                        parseUserList(body);
                        break;
                    case USER_NAME_LIST:
                        parseUserNameList(body);
                        break;
                    case UPLOAD_FILE:
                        uploadFile(body);
                        break;
                    case UPLOAD_FILE_SUCCESS:
                        uploadFileSuccess(body);
                        break;
                    case FILE_INFO:
                        receiveFileInfo(message.getFrom(), message.getTo(), body);
                        break;
                    case VIDEO_CHAT_REPLY:
                        handleVideoChatReply(message.getFrom(),body);
                        break;
                    case VIDEO_CHAT:
                        handleVideoChat(message.getFrom(),body);
                        break;
                }
            }
        }
        catch (SocketException e){

        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(byte[] data){
        if(data[0]==1){
            int index=data[1];
            FileSaver fileSaver=Content.currentDownloadFileMap.get(index);
            int part=(data[2]&0xff)<<24|(data[3]&0xff)<<16|(data[4]&0xff)<<8|(data[5]&0xff);
            try {
                fileSaver.write(part,data);
                if(fileSaver.isFinish()){
                    //TODO
                    System.out.println(fileSaver.getFileName()+"接收完成");
                    if(Content.privateChatHandler!=null){
                        msg = new android.os.Message();
                        msg.obj = fileSaver;
                        msg.what = Common.handler_fileReceiveSuccess;
                        Content.privateChatHandler.sendMessage(msg);
                    }
                    else{
                        msg = new android.os.Message();
                        msg.obj = fileSaver;
                        msg.what = Common.handler_fileReceiveSuccess;
                        Content.groupChatHandler.sendMessage(msg);
                    }
                    Content.currentDownloadFileMap.remove(index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void parseUserList(String body){
        String[] data=body.split(";");
        Content.userList.clear();
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            if(user.length<2)
                break;
            Content.userList.put(user[1], Integer.valueOf(user[0]));
        }
    }

    private void parseUserNameList(String body){
        String[] data=body.split(";");
        Content.idNameRecord.clear();
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            if(user.length<2)
                break;
            Content.idNameRecord.put( Integer.valueOf(user[0]),user[1]);
        }

        if (Content.privateHandler!=null){
            msg=new android.os.Message();
            msg.obj="update UserList";
            msg.what= Common.handler_updateUserList;
            Content.privateHandler.sendMessage(msg);
        }
        System.out.println(Content.idNameRecord);
    }

    private void uploadFile(String body){
        Content.onFile=true;
        int order= Integer.parseInt(body.substring(0,body.indexOf(";")));
        String fileName=body.substring(body.indexOf(";")+1);
        System.out.println("order is "+order);
        File file=Content.upLoadFileMap.get(fileName);
        if(file!=null){
            System.out.println("尝试发送"+file.getName()+"给服务器");
            byte[] data=new byte[Content.bytelength];
            int partNum= (int) (file.length()/Content.PART_BYTE+1);
            try {
                BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                byte[] fileBuffer=new byte[Content.PART_BYTE];
                int part_i=0;
                while(bin.read(fileBuffer)>0){
                    part_i++;
                    data[0]=1;
                    data[1]= (byte) order;
                    System.arraycopy(intTobyte(part_i),0,data,2,4);
                    System.arraycopy(fileBuffer,0,data,6,Content.PART_BYTE);
                    byte[] sizeAr = ByteBuffer.allocate(4).putInt(data.length).array();
                    output.write(sizeAr);
                    output.write(data);
                    output.flush();
                }
                bin.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }


        }
    }

    private void uploadFileSuccess(String body){
        System.out.println("文件"+body+"上传成功");
        Content.upLoadFileMap.remove(body);
        Content.onFile=false;
        if (Content.groupChatHandler!=null){
            msg=new android.os.Message();
            msg.obj="文件"+body+"传输成功";
            msg.what= Common.handler_sendFileSuccess;
            Content.groupChatHandler.sendMessage(msg);
        }

    }

    private void receiveFileInfo(int from,int to,String body){
        boolean isGroup= body.substring(0,body.indexOf(";")).equals("1");
        body=body.substring(body.indexOf(";")+1);
        long fileLength= Long.parseLong(body.substring(0,body.indexOf(";")));
        String filename=body.substring(body.indexOf(";")+1);
        if(isGroup){

            FileSaver fileSaver=new FileSaver(from,to,fileLength,filename,isGroup);
            if(Content.groupFileReceiveMap.get(from)==null)
                Content.groupFileReceiveMap.put(from,new HashMap<String,FileSaver>());
            Content.groupFileReceiveMap.get(from).put(filename,fileSaver);
            System.out.println("" + from + "尝试发送文件(群聊)" + filename + ",文件字节" + fileLength);
            if (Content.groupChatHandler!=null){
                msg=new android.os.Message();
                msg.obj=fileSaver;
                msg.what= Common.handler_fileInfo;
                Content.groupChatHandler.sendMessage(msg);
            }
       }
        else {
            FileSaver fileSaver = new FileSaver(from, to, fileLength, filename, isGroup);
            if (Content.privateFileReceiveMap.get(from) == null)
                Content.privateFileReceiveMap.put(from, new HashMap<String, FileSaver>());
            Content.privateFileReceiveMap.get(from).put(filename, fileSaver);
            System.out.println("" + from + "尝试发送文件(私聊)" + filename + ",文件字节" + fileLength);
            if (Content.privateChatHandler != null) {
                msg = new android.os.Message();
                msg.obj = fileSaver;
                msg.what = Common.handler_fileInfo;
                Content.privateChatHandler.sendMessage(msg);
            }
        }
    }

    private void handleVideoChatReply(int from,String body){
        if(Content.videoChatID==from){
            if(body.equals("ok")){
                System.out.println("开始视频通话");
                if (Content.mainHandler!=null){
                    msg=new android.os.Message();
                    msg.obj="开始视频通话";
                    msg.what= Common.handler_videoChatAccept;
                    Content.mainHandler.sendMessage(msg);

                Content.isVideo=true;
                Content.isVoice=true;
                new Thread(new VideoChatThread(Content.server,Content.port+1,client.getId(),from)).start();
                new Thread(new VoiceChatThread(Content.server,Content.port+2,client.getId(),from)).start();
                }
            }
            else if(body.equals("reject")){
                System.out.println("对方拒绝了视频通话");
                //聊天界面显示
                if (Content.privateChatHandler!=null){
                    msg=new android.os.Message();
                    msg.obj="对方拒绝了视频通话";
                    msg.what= Common.handler_videoChatReject;
                    Content.privateChatHandler.sendMessage(msg);
                }
            }
        }
        else{
            String[] str=body.split(";");
            System.out.println(str[1]);
        }
    }

    private void handleVideoChat(int from,String body){
        System.out.println("用户"+Content.idNameRecord.get(from)+"向你发起了视频通话");
        //聊天界面显示,能够选择接收或拒绝
        if (Content.mainHandler!=null){
            msg=new android.os.Message();
            msg.obj=from;
            msg.what= Common.handler_videoChatRequest;
            Content.mainHandler.sendMessage(msg);
        }

    }

    //int 转化为字节数组
    public static byte[] intTobyte(int num)
    {
        return new byte[] {(byte)((num>>24)&0xff),(byte)((num>>16)&0xff),(byte)((num>>8)&0xff),(byte)(num&0xff)};
    }
}
