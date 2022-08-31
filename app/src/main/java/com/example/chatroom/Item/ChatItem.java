package com.example.chatroom.Item;

import com.example.chatroom.Utils.FileSaver;

import java.io.File;

public class ChatItem {
    private Boolean fromOther;
    private String content;
    private String Name;
    private int ID;
    private FileSaver fileSaver=null;

    public ChatItem(Boolean fromOther, String content, String name, int ID) {
        this.fromOther = fromOther;
        this.content = content;
        Name = name;
        this.ID = ID;
    }

    public ChatItem(Boolean fromOther, String content, String name, int ID, FileSaver  fileSaver) {
        this.fromOther = fromOther;
        this.content = content;
        Name = name;
        this.ID = ID;
        this.fileSaver = fileSaver;
    }

    public Boolean getFromOther() {
        return fromOther;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return Name;
    }

    public int getID() {
        return ID;
    }

    public FileSaver getFileSaver() {
        return fileSaver;
    }
}