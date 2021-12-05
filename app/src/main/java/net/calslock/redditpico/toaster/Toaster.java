package net.calslock.redditpico.toaster;

import android.content.Context;
import android.widget.Toast;

//klasa pomocnicza do robienia tostów
public class Toaster {
    public static void makeToast(Context context, CharSequence text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
