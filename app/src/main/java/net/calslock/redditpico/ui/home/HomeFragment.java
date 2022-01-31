package net.calslock.redditpico.ui.home;

/*
https://api.reddit.com/subreddits/ zwraca subreddity najnowsze
 */

import static android.graphics.Bitmap.createScaledBitmap;

import android.annotation.SuppressLint;
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
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.HashMap;
import java.util.Map;

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
        int newWidth, newHeight;
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
                if(dstWidth == 0){
                    while (bmp.getHeight() > dstHeight){
                        newWidth = (int) (bmp.getHeight()*0.8);
                        newHeight = (int) (bmp.getWidth()*0.8);
                        bmp = createScaledBitmap(bmp, newWidth, newHeight, true);
                    }
                }
                else bmp = createScaledBitmap(bmp, dstWidth, dstHeight, true);
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
        String subreddit = itemData[0];
        String article = itemData[4].substring(3);
        try {
            url = "https://oauth.reddit.com/" + subreddit + "/comments/" + article;
            System.err.println("GET REQUEST ===> " + url);
            redditClient.get(url, access_token, null, new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONArray all = new JSONArray(result);
                        //article
                        JSONObject data = all.getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data");
                        String[] articleDataSet = {
                                data.getString("subreddit_name_prefixed"),//0
                                data.getString("score"),//1
                                "u/" + data.getString("author"),//2
                                data.getString("title"),//3
                                data.getString("name"),//4
                                data.getString("thumbnail"),//5
                                data.getString("selftext"),//6 //description
                        };
                        String picture;
                        try{
                            picture = data.getJSONObject("preview").getJSONArray("images").getJSONObject(0).getJSONObject("source").getString("url");
                            createPostBuilder(articleDataSet, picture);
                        }
                        catch (org.json.JSONException e) {
                            //e.printStackTrace();
                            createPostBuilder(articleDataSet, "");
                        }
                        //comments
                        /*
                        for (int i = 0; i < children.length(); i++) {
                            JSONObject childData = children.getJSONObject(i).getJSONObject("data");
                            String[] childDataSet = {
                                    childData.getString("subreddit_name_prefixed"),//0
                                    childData.getString("score"),//1
                                    "u/" + childData.getString("author"),//2
                                    childData.getString("title"),//3
                                    childData.getString("name"),//4
                                    childData.getString("thumbnail"),//5
                                    childData.getString("selftext"),//6 //description
                            };
                            dataSet.add(childDataSet);
                        }
                         */
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    Toaster.makeToast(mContext, "Couldn't connect to server!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
            }

    @SuppressLint("SetTextI18n")
    public void createPostBuilder(String[] dataSet, String picture){
        Log.i("pic", picture);
        builder = new MaterialAlertDialogBuilder(mContext);
        builder.setTitle(dataSet[3]);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.show_post_dialog, null);
        builder.setView(dialogView);

        TextView username, subreddit, karma, content;
        ImageButton upvote, downvote;
        ImageView image;

        username = (TextView) dialogView.findViewById(R.id.dialogAuthor);
        subreddit = (TextView) dialogView.findViewById(R.id.dialogSubreddit);
        karma = (TextView) dialogView.findViewById(R.id.dialogKarma);
        content = (TextView) dialogView.findViewById(R.id.dialogContent);
        upvote = (ImageButton) dialogView.findViewById(R.id.upVote);
        downvote = (ImageButton) dialogView.findViewById(R.id.downVote);
        image = (ImageView) dialogView.findViewById(R.id.dialogImage);

        username.setText(dataSet[2]);
        subreddit.setText(dataSet[0]);
        karma.setText(dataSet[1]);
        if(picture.equals("")){
            ((ViewManager)image.getParent()).removeView(image);
            content.setText(dataSet[6]);
        }
        else if(picture.contains("external-preview")){
            ((ViewManager)image.getParent()).removeView(image);
            content.setText("Unfortunately, this content is not available.");
        }
        else{
            picture = picture.replaceAll("amp;s", "s");
            ((ViewManager)content.getParent()).removeView(content);
            Bitmap bmp = getImageFromURL(picture, 0, 480);
            image.setImageBitmap(bmp);
        }

        upvote.setOnClickListener(view -> {
            vote(dataSet[4], 1);
        });

        downvote.setOnClickListener(view -> {
            vote(dataSet[4], -1);
        });


        //builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void vote(String id, int dir){
        String url = "https://oauth.reddit.com/api/vote?id=" + id + "&dir=" + dir;
        redditClient.post(url, access_token, null, null, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Toaster.makeToast(mContext, "Successfully voted!");
            }

            @Override
            public void onFailure(VolleyError error) {
                Toaster.makeToast(mContext, "Couldn't connect to servers!");
            }
        });
    }
}