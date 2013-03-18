package com.jason.lawgarden;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jason.util.JsonUtil;

public class LoginActivity extends Activity {

    private String mUserName;

    private String mPwd;

    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mCheckBox = (CheckBox) findViewById(R.id.chk_remember_pwd);
    }

    public void onBtnLoginClick(View view) {
        mUserName = ((EditText) findViewById(R.id.edit_user_name)).getText().toString();
        mPwd = ((EditText) findViewById(R.id.edit_pwd)).getText().toString();
        new MyAsyncTask().execute();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // JsonUtil.register();
                JsonUtil.sAccessToken = JsonUtil.login(mUserName, mPwd);
                if (mCheckBox.isChecked()) {

                }
                // JsonUtil.getUserSubjects(getApplicationContext());
                // JsonUtil.updateSubjects(getApplicationContext());
                // JsonUtil.updateNews(getApplicationContext());
                // JsonUtil.updateArticles(getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }
}
