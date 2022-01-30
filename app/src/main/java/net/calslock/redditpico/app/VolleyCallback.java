package net.calslock.redditpico.app;

import com.android.volley.VolleyError;

public interface VolleyCallback {
    void onSuccess(String result);
    void onFailure(VolleyError error);
}

//Obsługa:
/*    public void getInfo(){

        get(url, token, new VolleyCallback(){
            @Override
            public void onSuccess(String result){
             ... //tutaj rzeczy
            }
        });
    }

 */