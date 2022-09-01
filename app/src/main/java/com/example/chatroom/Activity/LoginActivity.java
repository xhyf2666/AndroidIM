package com.example.chatroom.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatroom.Client.Client;
import com.example.chatroom.R;
import com.example.chatroom.model.Common;
import com.example.chatroom.model.Content;

public class LoginActivity extends AppCompatActivity {
    private loginHandler handler;
    private Button loginButton;
    private Button registerButton;
    private EditText account_text;
    private EditText password_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton=(Button) findViewById(R.id.login);
        registerButton=(Button) findViewById(R.id.register);
        account_text =(EditText) findViewById(R.id.account_text);
        password_text=(EditText) findViewById(R.id.password_text);

        handler =new loginHandler();
        Content.loginHandler=handler;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=account_text.getText().toString();
                String password=password_text.getText().toString();


                if (userName.equals("") ||password.equals("")){
                    Toast toast=Toast.makeText(getBaseContext(), "账号或密码为空！", Toast.LENGTH_SHORT);
                    toast.show();
                }
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            Content.client=new Client(Content.server,Content.port);
                            Content.userName=userName;
                            Content.client.login(userName,password);

                            Thread.sleep(1000);
                            Content.client.getUserNameList();
                            Thread.sleep(1000);
                            Content.client.getUserList();
                        } catch (Exception e) {
                            Log.e("chatroom", e.getMessage());
                        }
                    }
                });
                thread.start();

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=account_text.getText().toString();
                String password=password_text.getText().toString();


                if (userName.equals("") || password.equals("")){
                    Toast toast=Toast.makeText(getBaseContext(), "账号或密码为空！", Toast.LENGTH_SHORT);
                    toast.show();
                }
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            Content.client=new Client(Content.server,Content.port);
                            Content.userName=userName;
                            Content.client.register(userName,password);

                            Thread.sleep(1000);
                            Content.client.getUserNameList();
                            Thread.sleep(1000);
                            Content.client.getUserList();
                        } catch (Exception e) {
                            Log.e("chatroom", e.getMessage());
                        }
                    }
                });
                thread.start();

            }
        });
    }

    class loginHandler extends Handler {
        // 通过覆写handlerMessage() 从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {
            String message=(String)msg.obj;
            switch (msg.what){
                case Common.handler_login:
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    LoginActivity.this.startActivity(intent);
                    break;
            }
        }
    }
}


