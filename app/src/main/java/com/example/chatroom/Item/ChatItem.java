package com.example.chatroom.Item;

public class ChatItem {
    private Boolean fromOther;
    private String content;
    private String Name;
    private int ID;

    public ChatItem(Boolean fromOther, String content, String name, int ID) {
        this.fromOther = fromOther;
        this.content = content;
        Name = name;
        this.ID = ID;
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
}