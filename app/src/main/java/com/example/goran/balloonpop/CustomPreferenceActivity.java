package com.example.goran.balloonpop;


import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Goran on 26.2.2017..
 */

public class CustomPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new CustomPreferenceFragment()).commit();

    }
}

