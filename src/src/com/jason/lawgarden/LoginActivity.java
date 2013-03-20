package com.jason.lawgarden;

import java.util.Date;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.User;
import com.jason.util.JsonUtil;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    private String mUserName;

    private String mPwd;

    private CheckBox mCheckBox;

    private DataBaseHelper mDbHelper;
    private EditText mEditUserName;
    private EditText mEditPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mCheckBox = (CheckBox) findViewById(R.id.chk_remember_pwd);
        mDbHelper = new DataBaseHelper(getApplicationContext());
        mDbHelper.openDataBase();

        mEditUserName = (EditText) findViewById(R.id.edit_user_name);
        mEditPwd = (EditText) findViewById(R.id.edit_pwd);
        new QueryPwdTask().execute();
    }

    public void onBtnLoginClick(View view) {
        mUserName = mEditUserName.getText().toString();
        mPwd = mEditPwd.getText().toString();
        new MyAsyncTask().execute();
    }

    private class QueryPwdTask extends AsyncTask<Void, Void, User> {

        @Override
        protected User doInBackground(Void... params) {
            return mDbHelper.getRememberedUser();
        }

        @Override
        protected void onPostExecute(User result) {
            if (result != null) {
                mEditUserName.setText(result.getUserName());
                mEditPwd.setText(result.getToken());
                mCheckBox.setChecked(true);
            }
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // JsonUtil.register();
                JsonUtil.sAccessToken = JsonUtil.login(mUserName, mPwd);
                if (TextUtils.isEmpty(JsonUtil.sAccessToken)) {
                    return false;
                }

                // insert into db
                User user = new User();
                user.setUserName(mUserName);
                user.setToken(mCheckBox.isChecked() ? JsonUtil.sAccessToken : "");
                user.setRememberPwd(mCheckBox.isChecked());
                user.setPurchaseDate(new Date());
                user.setOverdueDate(new Date());

                JsonUtil.sUser = mDbHelper.insertOrUpdateUser(user);
                JsonUtil.updateUserSubjects(getApplicationContext());
                JsonUtil.updateSubjects(getApplicationContext());

                JsonUtil.updateNews(getApplicationContext());
                JsonUtil.updateArticles(getApplicationContext());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "√‹¬Î¥ÌŒÛ", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
