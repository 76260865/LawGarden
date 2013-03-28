package com.jason.lawgarden;

import org.json.JSONException;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.util.JsonUtil;

public class SplashActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(this);
        dbHelper.testDb();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

}
