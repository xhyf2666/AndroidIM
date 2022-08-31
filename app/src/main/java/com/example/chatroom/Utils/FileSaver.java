package com.example.chatroom.Utils;

import com.example.chatroom.model.Content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSaver {
    private final int from;
    private final int to;
    private final long fileLength;
    private String location;
    private final int parts;
    private int current;
    private final String fileName;
    private FileOutputStream output;
    private final boolean isGroup;
    private String absolutePath;

    public FileSaver(int from, int to, long fileLength, String fileName, boolean isGroup) {
        this.from = from;
        this.to = to;
        this.fileLength = fileLength;
        this.fileName = fileName;
        this.isGroup = isGroup;
        current=0;
        parts= (int) (fileLength/ Content.PART_BYTE+1);

    }

    public void startSave(){
        if(isGroup){
            location="file/group/"+to+"/"+from+"/";
        }
        else{
            location="file/private/"+from+"/"+to+"/";
        }
        new File(location).mkdirs();

        location+=fileName;
        absolutePath =new File(location).getAbsolutePath();
        try {
            output=new FileOutputStream(location);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(int part,byte[] data) throws IOException {

        current++;
        if(current<parts) {
            output.write(data, 6, Content.PART_BYTE);
            output.flush();
        }
        else{
            output.write(data,6, (int) (fileLength- Content.PART_BYTE*(current-1)));
            output.flush();
            output.close();
        }
    }

    public boolean isFinish(){
        return current==parts;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public String getLocation() {
        return location;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
}
