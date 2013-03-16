package com.jason.lawgarden;

import org.json.JSONException;

import com.jason.util.JsonUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        new MyAsyncTask().execute();

    }

    public void onBtnLoginClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

//            try {
                // JsonUtil.register();
//                JsonUtil.sAccessToken = JsonUtil.login("jason", "123456");
//                JsonUtil.getUserSubjects(getApplicationContext());
                // JsonUtil.updateSubjects(getApplicationContext());
                // JsonUtil.updateNews(getApplicationContext());
                // JsonUtil.updateArticles(getApplicationContext());
//            } catch (JSONException e) {
                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            return null;
        }

    }
}
