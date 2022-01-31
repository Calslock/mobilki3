package net.calslock.redditpico.ui.home;

/*
https://api.reddit.com/subreddits/ zwraca subreddity najnowsze
 */

import static android.graphics.Bitmap.createScaledBitmap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import net.calslock.redditpico.MainMenuAdapter;
import net.calslock.redditpico.R;
import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.app.VolleyCallback;
import net.calslock.redditpico.databinding.FragmentHomeBinding;
import net.calslock.redditpico.room.TokenDao;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.room.TokenRoomDatabase;
import net.calslock.redditpico.toaster.Toaster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class HomeFragment extends Fragment implements MainMenuAdapter.ItemClickListener{

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
    RecyclerView recyclerView;
    MainMenuAdapter mainMenuAdapter;
    Thread t;
    HomeFragment homeFragment;
    MaterialAlertDialogBuilder builder;
    //AlertDialog.Builder builder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        homeFragment = this;

        tokenRoomDatabase = TokenRoomDatabase.getDatabase(mContext);
        tokenDao = tokenRoomDatabase.tokenDao();

        redditClient = new RedditClient(mContext);
        recyclerView = binding.recyclerMain;
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        this.getUserInfo();
        try{
            t.join();
            this.populateContent();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
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
            t = new Thread(() -> {
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
                            Bitmap avatar = getImageFromURL(imageurl, 196, 196);
                            if (avatar != null) {
                                sideBarAvatar.setImageBitmap(avatar);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(VolleyError e){}
                });
            });
            t.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Bitmap getImageFromURL(String avatarURL, int dstWidth, int dstHeight){
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
                bmp = createScaledBitmap(bmp, dstWidth, dstHeight, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    public void populateContent() {
        ArrayList<String[]> dataSet = new ArrayList<>();

        new Thread(() -> {
            url = "https://oauth.reddit.com/best";
            token = tokenDao.getToken(0);
            access_token = token.getToken();
            redditClient.get(url, access_token, null, new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject data = new JSONObject(result);
                        JSONArray children = data.getJSONObject("data").getJSONArray("children");
                        for (int i = 0; i < children.length(); i++) {
                            JSONObject childData = children.getJSONObject(i).getJSONObject("data");
                            String[] childDataSet = {
                                    childData.getString("subreddit_name_prefixed"),
                                    childData.getString("score"),
                                    "u/" + childData.getString("author"),
                                    childData.getString("title"),
                                    childData.getString("name")
                            };
                            dataSet.add(childDataSet);
                        }
                        String[][] finalDataSet = dataSet.toArray(new String[][]{});
                        mainMenuAdapter = new MainMenuAdapter(finalDataSet);
                        mainMenuAdapter.setClickListener(homeFragment);
                        recyclerView.setAdapter(mainMenuAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    Toaster.makeToast(mContext, "Couldn't connect to server!");
                }
            });
        }).start();
    }

    @Override
    public void onItemClick(View view, int position, String[] itemData) {
        Toaster.makeToast(mContext, "Clicked on id:"+ position);
        builder = getArticle(view.getContext(), itemData);
        builder.show();

    }

    public MaterialAlertDialogBuilder getArticle(Context context, String[] data) {
        ArrayList<String[]> dataSet = new ArrayList<>();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        String subreddit = data[0];
        String article = data[4].substring(3);
        new Thread(() -> {

            url = "https://oauth.reddit.com/"+subreddit+"/comments/"+article;
            System.err.println("GET REQUEST ===> "+url);
            token = tokenDao.getToken(0);
            access_token = token.getToken();
            redditClient.get(url, access_token, null, new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject data = new JSONObject(result);
                        JSONArray children = data.getJSONObject("data").getJSONArray("children");
                        for (int i = 0; i < children.length(); i++) {
                            JSONObject childData = children.getJSONObject(i).getJSONObject("data");
                            String[] childDataSet = {
                                    childData.getString("subreddit_name_prefixed"),//0
                                    childData.getString("score"),//1
                                    "u/" + childData.getString("author"),//2
                                    childData.getString("title"),//3
                                    childData.getString("name"),//4
                                    childData.getString("thumbnail"),//5
                            };
                            dataSet.add(childDataSet);
                        }
                        String[][] finalDataSet = dataSet.toArray(new String[][]{});
                        mainMenuAdapter = new MainMenuAdapter(finalDataSet);
                        mainMenuAdapter.setClickListener(homeFragment);
                        recyclerView.setAdapter(mainMenuAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    Toaster.makeToast(mContext, "Couldn't connect to server!");
                }
            });
        }).start();

        builder.setTitle(article);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.show_post_dialog, null);
        builder.setView(dialogView);

        builder.setNegativeButton("Zamknij", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder;
    }
}