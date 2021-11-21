package net.calslock.redditpico;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.config.Auth;

import org.json.JSONException;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    boolean authComplete = false;
    String DEVICE_ID = UUID.randomUUID().toString();
    WebView web;
    SharedPreferences pref;
    Dialog auth_dialog;
    String authCode;
    Intent resultIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("reddit-pico");

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

    }

    public void goToMain(View v){
        auth_dialog = new Dialog(LoginActivity.this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        web = (WebView) auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);

        String url = Auth.OAUTH_URL + "?client_id=" + Auth.CLIENT_ID + "&response_type=code&state="+DEVICE_ID+"&redirect_uri=" + Auth.REDIRECT_URI + "&scope=" + Auth.OAUTH_SCOPE;

        web.loadUrl(url);

        Toast.makeText(getApplicationContext(), "" + url, Toast.LENGTH_LONG).show();

        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") || url.contains("&code=")) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    LoginActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("Code", authCode);
                    edit.apply();
                    auth_dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Authorization Code is: " + pref.getString("Code", ""), Toast.LENGTH_SHORT).show();

                    try {
                        new RedditClient(getApplicationContext()).getToken(Auth.TOKEN_URL, Auth.GRANT_TYPE2, DEVICE_ID);
                        Toast.makeText(getApplicationContext(), "Success Token: " + pref.getString("token", ""), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainBoardActivity.class);
                        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize");
        auth_dialog.setCancelable(true);

        //Intent intent = new Intent(getApplicationContext(), MainBoardActivity.class);
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        //startActivity(intent);
    }
}