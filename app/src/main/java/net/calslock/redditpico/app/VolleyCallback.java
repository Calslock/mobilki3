package net.calslock.redditpico.app;

import org.json.JSONObject;

public interface VolleyCallback {
    void onSuccess(String result);
    void onFailure();
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