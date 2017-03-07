package com.example.goran.balloonpop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;

/**
 * Created by Goran on 26.2.2017..
 */

public class MainActivity extends AppCompatActivity  {


    private PopOnView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        view = new PopOnView(this, getPreferences(Context.MODE_PRIVATE), layout);
        layout.addView(view, 0);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menuSettings) {
            Intent settingsIntent = new Intent(this, CustomPreferenceActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        view.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        view.resume(this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

}
