package net.calslock.redditpico.ui.account;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.VolleyError;

import net.calslock.redditpico.R;
import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.app.VolleyCallback;
import net.calslock.redditpico.databinding.FragmentAccountBinding;
import net.calslock.redditpico.room.TokenDao;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.room.TokenRoomDatabase;
import net.calslock.redditpico.ui.home.HomeFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    String url, access_token, username, imageurl;
    TokenEntity token;
    TokenDao tokenDao;
    TokenRoomDatabase tokenRoomDatabase;
    RedditClient redditClient;
    String[] karmaTable;
    ImageView accAvatar;
    TextView[] accKarma;
    TextView accName, accDescription, accFollowers, accTitle;
    AlertDialog.Builder alertFollowers;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tokenRoomDatabase = TokenRoomDatabase.getDatabase(mContext);
        tokenDao = tokenRoomDatabase.tokenDao();

        redditClient = new RedditClient(mContext);

        getUserInfo();

        /*final TextView textView = binding.textGallery;
        accountViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
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

    public void getUserInfo(){
        try {
            new Thread(() -> {
                alertFollowers = new AlertDialog.Builder(mContext);
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
                            username = "u/" + userData.getString("name");
                            imageurl = userData.getString("icon_img").split("\\?")[0];
                            karmaTable = new String[]{userData.getString("link_karma"), userData.getString("comment_karma"), userData.getString("awardee_karma"), userData.getString("awarder_karma")};

                            accName = (TextView) getView().findViewById(R.id.accInfoName);
                            accAvatar = (ImageView) getView().findViewById(R.id.accInfoAvatar);
                            accKarma = new TextView[]{getView().findViewById(R.id.accPostKarma), getView().findViewById(R.id.accCommentKarma), getView().findViewById(R.id.accAwardeeKarma), getView().findViewById(R.id.accAwarderKarma)};
                            accTitle = (TextView) getView().findViewById(R.id.accInfoTitle);

                            accDescription = (TextView) getView().findViewById(R.id.accDescription);
                            accDescription.setText(userData.getJSONObject("subreddit").getString("public_description"));
                            accFollowers = (TextView) getView().findViewById(R.id.accFollowers);
                            accFollowers.setText(userData.getJSONObject("subreddit").getString("subscribers") + " followers >");
                            accFollowers.setOnClickListener(view -> {
                                alertFollowers.setTitle("Followers");
                                LayoutInflater inflater = getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.followers_dialog, null);
                                alertFollowers.setView(dialogView);
                                alertFollowers.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                                alertFollowers.show();
                            });


                            accName.setText(username);
                            accTitle.setText(userData.getJSONObject("subreddit").getString("title"));
                            for (int i = 0; i < karmaTable.length; i++){
                                accKarma[i].setText(karmaTable[i]);
                            }
                            Bitmap avatar = HomeFragment.getAvatar(imageurl);
                            if (avatar != null) {
                                accAvatar.setImageBitmap(avatar);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(VolleyError e){}
                });
            }).start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
}