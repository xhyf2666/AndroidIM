package com.example.chatroom.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatroom.Item.ChatItem;
import com.example.chatroom.R;
import com.example.chatroom.Utils.FileSaver;

import java.util.List;


public class ChatAdapter extends ArrayAdapter<ChatItem> {
    private  int resourceId;
    public ChatAdapter(Context context, int textViewResourceId, List<ChatItem> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ChatItem item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView from = view.findViewById(R.id.chat_person_from);
        TextView contentOther = view.findViewById(R.id.chat_contentOther);
        TextView contentSelf = view.findViewById(R.id.chat_contentSelf);
        ImageView self = view.findViewById(R.id.chat_person_self);
        ImageView bubbleOther =view.findViewById(R.id.bubbleOther);
        ImageView bubbleSelf =view.findViewById(R.id.bubbleSelf);
        TextView groupUserNameSelf = view.findViewById(R.id.groupUserNameSelf);
        TextView groupUserNameOther = view.findViewById(R.id.groupUserNameOther);
        //来自其他人
        if(item.getFromOther()){
            bubbleOther.setVisibility(View.VISIBLE);
            bubbleSelf.setVisibility(View.INVISIBLE);
            from.setVisibility(View.VISIBLE);
            self.setVisibility(View.INVISIBLE);
            contentSelf.setVisibility(View.INVISIBLE);
            groupUserNameSelf.setVisibility(View.INVISIBLE);
        }else{
            bubbleOther.setVisibility(View.INVISIBLE);
            bubbleSelf.setVisibility(View.VISIBLE);
            from.setVisibility(View.INVISIBLE);
            self.setVisibility(View.VISIBLE);
            contentOther.setVisibility(View.INVISIBLE);
            groupUserNameOther.setVisibility(View.INVISIBLE);
        }
        contentSelf.setText(item.getContent());
        contentOther.setText(item.getContent());
        groupUserNameSelf.setText(item.getName());
        groupUserNameOther.setText(item.getName());
        if (item.getFileSaver()!=null){
            FileSaver fileSaver= item.getFileSaver();
            TextView fileFrom=view.findViewById(R.id.groupFileInfoFrom);
            TextView fileTo=view.findViewById(R.id.groupFileInfoTo);
            TextView fileLength=view.findViewById(R.id.groupFileInfoLength);
            TextView fileName=view.findViewById(R.id.groupFileInfoName);
            fileFrom.setText(""+fileSaver.getFrom());
            fileTo.setText(""+fileSaver.getTo());
            fileLength.setText(""+fileSaver.getFileLength());
            fileName.setText(fileSaver.getFileName());
            contentOther.setText("文件:"+item.getContent());
        }

        return  view;
    }
}