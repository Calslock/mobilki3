package net.calslock.redditpico;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.room.TokenDao;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.room.TokenRoomDatabase;
import net.calslock.redditpico.toaster.Toaster;

public class LoginActivity extends AppCompatActivity {

    TokenDao tokenDao;
    TokenRoomDatabase tkDatabase;
    RedditClient redditClient;

    String login, password, access_token;
    EditText loginbox, passwordbox;
    TokenEntity token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("reddit-pico");
        tkDatabase = TokenRoomDatabase.getDatabase(getApplicationContext());
        tokenDao = tkDatabase.tokenDao();
        redditClient = new RedditClient(getApplicationContext());
        loginbox = (EditText) findViewById(R.id.loginUsername);
        passwordbox = (EditText) findViewById(R.id.loginPassword);
    }

    public void goToMain(View v){
        login = loginbox.getText().toString();
        password = passwordbox.getText().toString();
        try {
            access_token = redditClient.getToken(login,password);
            if (access_token != null) {
                token = new TokenEntity(0, access_token);
                new Thread(() -> {
                    tokenDao.delete();
                    tokenDao.insert(token);
                    Intent intent = new Intent(getApplicationContext(), MainBoardActivity.class);
                    startActivity(intent);
                }).start();
                this.finish();
            }
        }catch(Exception e){
            Toaster.makeToast(getApplicationContext(), "Connection error");
        }
    }
}