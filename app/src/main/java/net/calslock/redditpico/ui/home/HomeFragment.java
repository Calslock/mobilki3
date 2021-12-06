package net.calslock.redditpico.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import net.calslock.redditpico.MainBoardActivity;
import net.calslock.redditpico.R;
import net.calslock.redditpico.app.RedditClient;
import net.calslock.redditpico.app.VolleyCallback;
import net.calslock.redditpico.databinding.FragmentHomeBinding;
import net.calslock.redditpico.room.TokenDao;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.room.TokenRoomDatabase;
import net.calslock.redditpico.toaster.Toaster;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    RedditClient redditClient;
    private String userInfo;
    String url, access_token;
    TokenEntity token;
    TokenDao tokenDao;
    TokenRoomDatabase tokenRoomDatabase;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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
                    redditClient.get(url, access_token, new VolleyCallback() {
                        @Override
                        public void onSuccess(String userInfo) {
                            Log.i("Data", userInfo);
                            //tutaj reszta maina
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
}