package net.calslock.redditpico.app;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.calslock.redditpico.config.Auth;
import net.calslock.redditpico.room.TokenDao;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.room.TokenRoomDatabase;
import net.calslock.redditpico.toaster.Toaster;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RedditClient {
    String access_token;
    Context context;
    String getResponse;

    public RedditClient(Context context) {
        this.context = context;
    }

    public void getToken(String login, String password, final VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Auth.AUTH_URL,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        access_token = res.getString("access_token");
                        callback.onSuccess(access_token);
                        //Toaster.makeToast(context, access_token);
                    } catch (JSONException j) {
                        j.printStackTrace();
                    }
                },
                error -> access_token = null){
            @Override
            //parametry żądania
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("grant_type", Auth.GRANT_TYPE2);
                params.put("username", login);
                params.put("password", password);
                return params;
            }
            @Override
            //nagłówki żądania
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String,String> headers = new HashMap<String, String>();
                String credentials = String.format("%s:%s", Auth.CLIENT_ID, Auth.CLIENT_SECRET);
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(stringRequest);
    }
    //Uniwersalna funkcja GET
    //url - URL do API
    //token - token z konta
    //VolleyCallback
    public void get(String url, String token, Map<String, String> addHeaders, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        callback.onSuccess(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> callback.onFailure()) {
            @Override
            //nagłówki żądania
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String auth = "bearer " + token;
                headers.put("Authorization", auth);
                if(addHeaders!=null && !addHeaders.isEmpty()){
                    addHeaders.forEach(headers::put);
                }
                return headers;
            }
        };
        queue.add(stringRequest);
    }
}
