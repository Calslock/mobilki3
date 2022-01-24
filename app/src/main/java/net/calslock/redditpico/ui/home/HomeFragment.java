package net.calslock.redditpico.ui.home;

/*
https://api.reddit.com/subreddits/ zwraca subreddity najnowsze
 */

import static android.graphics.Bitmap.createScaledBitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;

import net.calslock.redditpico.MainBoardActivity;
import net.calslock.redditpico.R;
import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.app.VolleyCallback;
import net.calslock.redditpico.databinding.FragmentHomeBinding;
import net.calslock.redditpico.room.TokenDao;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.room.TokenRoomDatabase;
import net.calslock.redditpico.toaster.Toaster;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    RedditClient redditClient;
    String url, access_token;
    TokenEntity token;
    TokenDao tokenDao;
    TokenRoomDatabase tokenRoomDatabase;
    String username, karma, imageurl;
    TextView sideBarName, sideBarKarma;
    ImageView sideBarAvatar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        tokenRoomDatabase = TokenRoomDatabase.getDatabase(mContext);
        tokenDao = tokenRoomDatabase.tokenDao();

        redditClient = new RedditClient(mContext);

        this.getUserInfo();


        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public void getUserInfo(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    url = "https://oauth.reddit.com/api/v1/me";
                    token = tokenDao.getToken(0);
                    access_token = token.getToken();
                    redditClient.get(url, access_token, null, new VolleyCallback() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(String userInfo) {
                            Log.i("token", access_token);
                            Log.i("Data", userInfo);
                            try {
                                JSONObject userData = new JSONObject(userInfo);
                                username = userData.getString("name");
                                karma = userData.getString("total_karma");
                                imageurl = userData.getString("icon_img").split("\\?")[0];
                                Log.i("url", imageurl);

                                NavigationView navigationView = (NavigationView) requireActivity().findViewById(R.id.nav_view);
                                View headerView = navigationView.getHeaderView(0);
                                sideBarName = (TextView) headerView.findViewById(R.id.sideBarName);
                                sideBarKarma = (TextView) headerView.findViewById(R.id.sideBarKarma);
                                sideBarAvatar = (ImageView) headerView.findViewById(R.id.sideBarAvatar);

                                sideBarName.setText(username);
                                sideBarKarma.setText("Karma: " + karma);
                                Bitmap avatar = getAvatar(imageurl);
                                if (avatar != null) {
                                    sideBarAvatar.setImageBitmap(avatar);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(){}
                    });
                }
            }).start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Bitmap getAvatar(String avatarURL){
        InputStream in;
        Bitmap bmp = null;
        int responseCode;
        try {
            URL url = new URL(avatarURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoInput(true);
            con.connect();
            responseCode = con.getResponseCode();
            if(responseCode == HttpsURLConnection.HTTP_OK)
            {
                in = con.getInputStream();
                bmp = BitmapFactory.decodeStream(in);
                in.close();
                bmp = createScaledBitmap(bmp, 196, 196, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }
}