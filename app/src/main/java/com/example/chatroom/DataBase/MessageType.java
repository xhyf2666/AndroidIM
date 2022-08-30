package com.example.chatroom.DataBase;


public enum MessageType {
    SUCCESS,FAIL,
    LOGIN,LOGOUT,REGISTER,//登录，登出，注册
    GROUP_MSG,PRIVATE_MSG,//群聊消息，私聊消息
    GROUP_MSG_LOG,PRIVATE_MSG_LOG,//群聊消息记录，私聊消息记录
    USER_LIST,USER_NAME_LIST,//用户列表
    GROUP_CREATE,GROUP_DELETE,GROUP_JOIN,GROUP_WITHDRAW,//创建、删除、加入、退出群聊
    FILE_INFO,UPLOAD_FILE,UPLOAD_FILE_SUCCESS,RECEIVE_FILE,//发送文件
    VIDEO_CHAT, VIDEO_CHAT_REPLY,//视频聊天，视频聊天回复
}