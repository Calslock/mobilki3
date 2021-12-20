package net.calslock.redditpico;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.app.VolleyCallback;
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
    LoginActivity activity = this;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SettingsActivity.SettingsFragment.updateTheme(prefs.getString("theme", "sysdef"));
    }

    public void goToMain(View v){
        login = loginbox.getText().toString();
        password = passwordbox.getText().toString();
        try {
            redditClient.getToken(login, password, new VolleyCallback() {
                @Override
                public void onSuccess(String access_token) {
                    if (access_token != null) {
                        token = new TokenEntity(0, access_token);
                        new Thread(() -> {
                            tokenDao.delete();
                            tokenDao.insert(token);
                            Intent intent = new Intent(getApplicationContext(), MainBoardActivity.class);
                            startActivity(intent);
                        }).start();
                        activity.finish();
                    }
                }
                @Override
                public void onFailure() {
                    Toaster.makeToast(getApplicationContext(), "Connection error");
                }
            });

        }catch(Exception e){
            Toaster.makeToast(getApplicationContext(), "Connection error");
        }
    }
}