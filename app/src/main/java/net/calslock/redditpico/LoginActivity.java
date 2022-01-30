package net.calslock.redditpico;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.VolleyError;

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
    SharedPreferences prefs;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch rememberSwitch;
    SharedPreferences.Editor editor;
    Context context = this;

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
        rememberSwitch = (Switch) findViewById(R.id.remSwitch);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        SettingsActivity.SettingsFragment.updateTheme(prefs.getString("theme", "sysdef"));

        access_token = prefs.getString("token", "nontoken");
        if (!access_token.equals("nontoken")) tryAutoLogin(access_token);
    }

    public void tryLogin(View v){
        login = loginbox.getText().toString();
        password = passwordbox.getText().toString();
        try {
            redditClient.getToken(login, password, new VolleyCallback() {
                @Override
                public void onSuccess(String access_token) {
                    if (access_token != null) {
                        if(rememberSwitch.isChecked()){
                            editor.putString("token", access_token);
                            editor.commit();
                        }
                        goToMain(access_token);
                    }
                }
                @Override
                public void onFailure(VolleyError e) {
                    Toaster.makeToast(getApplicationContext(), "Connection error");
                }
            });

        }catch(Exception e){
            Toaster.makeToast(getApplicationContext(), "Connection error");
        }
    }

    public void tryAutoLogin(String access_token){
        String url = "https://oauth.reddit.com/api/v1/me";
        redditClient.get(url, access_token, null, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Toaster.makeToast(context, "Autosigning...");
                goToMain(access_token);
            }
            @Override
            public void onFailure(VolleyError e) {
                if (e.networkResponse.statusCode == 401) Toaster.makeToast(context, "The token has expired, please log in again");
                else Toaster.makeToast(context, "We made a booboo, please wait while we fix ;-;");
            }
        });
    }

    public void goToMain(String access_token){
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