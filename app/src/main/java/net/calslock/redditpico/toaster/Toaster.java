package net.calslock.redditpico.toaster;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IdRes;

//klasa pomocnicza do robienia tostów
public class Toaster {
    public static void makeToast(Context context, CharSequence text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
