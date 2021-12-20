package net.calslock.redditpico;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.setTitle("Settings");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ListPreference themePreference = findPreference("theme");
            assert themePreference != null;
            updateTheme(themePreference.getValue());
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                updateTheme(newValue.toString());
                return true;
            });
        }

        public void updateTheme(String newValue){
            int targetTheme;
            switch (newValue) {
                default:
                case "sysdef":
                    targetTheme = MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
                case "dark":
                    targetTheme = MODE_NIGHT_YES;
                    break;
                case "light":
                    targetTheme = MODE_NIGHT_NO;
                    break;
            }
            AppCompatDelegate.setDefaultNightMode(targetTheme);
        }
    }
}