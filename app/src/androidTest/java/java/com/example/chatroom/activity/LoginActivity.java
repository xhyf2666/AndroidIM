package java.com.example.chatroom.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatroom.Client.Client;
import com.example.chatroom.R;
import com.example.chatroom.model.Content;

public class LoginActivity extends AppCompatActivity {

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
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=account_text.getText().toString();
                String password=password_text.getText().toString();


                if (userName.equals("") ||password.equals("")){
                    Toast toast=Toast.makeText(getBaseContext(), "账号或密码为空！", Toast.LENGTH_SHORT);
                    toast.show();
                }
                Content.client=new Client(Content.server,Content.port);
                Content.userName=userName;
                Content.client.login(userName,password);
            }
        });
    }


}