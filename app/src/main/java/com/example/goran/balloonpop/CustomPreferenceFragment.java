package com.example.goran.balloonpop;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import static com.example.goran.balloonpop.PopOnView.HIGH_SCORE;
import static com.example.goran.balloonpop.PopOnView.preferences;

/**
 * Created by Goran on 26.2.2017..
 */

public class CustomPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference resetPref = findPreference("resetKEY");
        resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE, 0);
                editor.apply();
                PopOnView.highScore = 0;
                Toast.makeText(getActivity(), "High score set to 0",
                        Toast.LENGTH_LONG).show();


                return true;
            }

        });

        Preference easyPref = findPreference("easyKEY");
        easyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                PopOnView.INITIAL_BALLOON = 4;
                PopOnView.score -= 10;
                PopOnView.MAX_LIVES=7;
                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                Toast.makeText(getActivity(), "Difficulty set to EASY",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

        });
        Preference mediumPref = findPreference("mediumKEY");
        mediumPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                PopOnView.INITIAL_BALLOON = 6;
                PopOnView.score -= 15;
                PopOnView.MAX_LIVES=6;
                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                Toast.makeText(getActivity(), "Difficulty set to MEDIUM",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

        });
        Preference hardPref = findPreference("hardKEY");
        hardPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                PopOnView.INITIAL_BALLOON = 8;
                PopOnView.score -= 20 * PopOnView.level;
                PopOnView.MAX_LIVES=5;
                PopOnView.BALLOON_DELAY=350;
                SharedPreferences.Editor editor = preferences.edit();
                editor.apply();
                Toast.makeText(getActivity(), "Difficulty set to HARD",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

        });
    }
}



