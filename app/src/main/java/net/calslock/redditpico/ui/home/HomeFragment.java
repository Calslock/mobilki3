package net.calslock.redditpico.ui.home;

import android.content.Intent;
import android.os.Bundle;
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
import net.calslock.redditpico.databinding.FragmentHomeBinding;
import net.calslock.redditpico.room.TokenEntity;
import net.calslock.redditpico.toaster.Toaster;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    RedditClient redditClient;
    private String userInfo;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        redditClient = new RedditClient(getContext());
        try {
            userInfo = redditClient.getUserInfo();
            System.out.println("UserInfo: "+userInfo);
        }catch(Exception e){
            e.printStackTrace();
        }

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
}