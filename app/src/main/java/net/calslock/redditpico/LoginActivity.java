package net.calslock.redditpico;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import net.calslock.redditpico.config.Auth;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    Auth auth;

    boolean authComplete = false;
    String DEVICE_ID = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("reddit-pico");
    }

    public void goToMain(View v){
        auth = new Auth();
        String url = auth.OAUTH_URL + "?client_id=" + auth.CLIENT_ID + "&response_type=code&state=TEST&redirect_uri=" + auth.REDIRECT_URI + "&scope=" + auth.OAUTH_SCOPE;
        Toast.makeText(getApplicationContext(), "" + url, Toast.LENGTH_LONG).show();
        //Intent intent = new Intent(getApplicationContext(), MainBoardActivity.class);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}