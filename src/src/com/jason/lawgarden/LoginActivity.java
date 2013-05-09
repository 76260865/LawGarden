package com.jason.lawgarden;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.User;
import com.jason.util.JsonUtil;
import com.jason.util.NetworkUtil;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    private static final String EXTRA_KEY_SHARE_REFS = "extra_key_share_refs";

    private static final String EXTRA_KEY_IS_LOGINED = "extra_key_is_logined";

    private String mUserName;

    private String mPwd;

    private CheckBox mCheckBox;

    private DataBaseHelper mDbHelper;
    private EditText mEditUserName;
    private EditText mEditPwd;

    private SharedPreferences mPrefs;

    private ImageView mImgWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        mCheckBox = (CheckBox) findViewById(R.id.chk_remember_pwd);
        mImgWelcome = (ImageView) findViewById(R.id.img_welcome);

        mEditUserName = (EditText) findViewById(R.id.edit_user_name);
        mEditPwd = (EditText) findViewById(R.id.edit_pwd);
        mPrefs = getSharedPreferences(EXTRA_KEY_SHARE_REFS, Context.MODE_PRIVATE);
        mDbHelper = DataBaseHelper.getSingleInstance(LoginActivity.this);
        new QueryPwdTask().execute();
    }

    private ProgressDialog mProgressDialog;
    private boolean mIsCaneled = false;
    private TextView mTxtLoadingInfo;
    private Button mBtnOk;
    private Button mBtnCancel;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressLogin;
    private MyAsyncTask mMyAsyncTask;

    public void onBtnLoginClick(View view) {
        if (!NetworkUtil.isNetworkConnected(this)) {
            Toast.makeText(getApplicationContext(), "请先链接网络", Toast.LENGTH_SHORT).show();
            return;
        }
        mUserName = mEditUserName.getText().toString();
        mPwd = mEditPwd.getText().toString();

        if (mProgressDialog == null) {
            initDialog();
        } else if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mTxtLoadingInfo.setText("正在登陆...");
        new LoginPwdTask().execute();
    }

    private void initDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.loading_dialog_layout);
        mBtnOk = (Button) mProgressDialog.findViewById(R.id.btn_ok);
        mBtnCancel = (Button) mProgressDialog.findViewById(R.id.btn_cancel);
        mTxtLoadingInfo = (TextView) mProgressDialog.findViewById(R.id.txt_loading_info);
        mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progress_loading);
        mProgressLogin = (ProgressBar) mProgressDialog.findViewById(R.id.progress_login);

        mBtnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mBtnOk.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mBtnCancel.setVisibility(View.VISIBLE);
                mProgressLogin.setVisibility(View.GONE);

                mMyAsyncTask = new MyAsyncTask();
                mMyAsyncTask.execute();
            }
        });
        mBtnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsCaneled = true;
                if (mMyAsyncTask == null) {
                    mProgressDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mMyAsyncTask.cancel(true);
                }
                Log.d("LoginActivity", "cancel");
            }
        });
    }

    public void onRegisterClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private class QueryPwdTask extends AsyncTask<Void, Void, User> {

        private boolean mHasUpdateData = false;

        @Override
        protected void onProgressUpdate(Void... values) {
            if (mProgressDialog == null) {
                initDialog();
            }
            mTxtLoadingInfo.setText("正在同步服务器。。。");
        }

        @Override
        protected User doInBackground(Void... params) {
            JsonUtil.sUser = mDbHelper.getRememberedUser();
            if (JsonUtil.sUser != null) {
                JsonUtil.sAccessToken = JsonUtil.sUser.getToken();
            } else {
                return null;
            }
            if (NetworkUtil.isNetworkConnected(getApplicationContext())) {
                // publishProgress();
                try {
                    JSONObject object = JsonUtil.ValidateToken(getApplicationContext());

                    if (object == null) {
                        return null;
                    }
                    if (object.getBoolean("ExecutionResult")) {
                        if (!object.getBoolean("Valid")) {
                            // token is invalid
                            return null;
                        } else {
                            JsonUtil.updateUserSubjects(getApplicationContext());
                        }
                    }

                    JsonUtil.updateUserSubjects(getApplicationContext());
                    mHasUpdateData = JsonUtil.CheckAllUpdates(getApplicationContext());
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            return mDbHelper.getRememberedUser();
        }

        @Override
        protected void onPostExecute(User result) {
            if (result != null) {
                JsonUtil.sUser = result;

                mEditUserName.setText(result.getUserName());
                mEditPwd.setText(result.getToken());
                mCheckBox.setChecked(true);

                if (mHasUpdateData) {
                    if (mProgressDialog == null) {
                        initDialog();
                    }
                    mBtnOk.setVisibility(View.VISIBLE);
                    mBtnCancel.setVisibility(View.VISIBLE);
                    mTxtLoadingInfo.setText("当前应用有更新，是否需要更新?");
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return;
            } else {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                mImgWelcome.setVisibility(View.GONE);
            }
        }
    }

    private class LoginPwdTask extends AsyncTask<Void, Void, String> {

        private boolean mHasUpdateData = false;

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject object = JsonUtil.login(mUserName, mPwd);
                if (object == null) {
                    return "服务器异常";
                }
                if (object.getBoolean("ExecutionResult")) {
                    JsonUtil.sAccessToken = object.getString("AccessToken");
                } else {
                    return object.getString("Message");
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            if (!TextUtils.isEmpty(JsonUtil.sAccessToken)) {
                mPrefs.edit().putBoolean(EXTRA_KEY_IS_LOGINED, true).commit();
            }

            // insert into db
            User user = new User();
            user.setUserName(mUserName);
            user.setToken(mCheckBox.isChecked() ? JsonUtil.sAccessToken : "");
            user.setRememberPwd(mCheckBox.isChecked());
            user.setPurchaseDate(new Date());
            user.setOverdueDate(new Date());

            JsonUtil.sUser = mDbHelper.insertOrUpdateUser(user);
            try {
                JsonUtil.updateUserSubjects(getApplicationContext());
            } catch (JSONException e1) {
                Log.e(TAG, e1.getMessage());
            }

            try {
                mHasUpdateData = JsonUtil.CheckAllUpdates(getApplicationContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mHasUpdateData) {
                mTxtLoadingInfo.setText("当前应用有更新数据，你是否要更新？");
                mProgressLogin.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mBtnOk.setVisibility(View.VISIBLE);
                mBtnCancel.setVisibility(View.VISIBLE);

            } else {
                mProgressDialog.dismiss();
                if (!TextUtils.isEmpty(result)) {
                    Toast.makeText(getApplicationContext(), result + "", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    class Progress {
        int progress;
        int total = 100;
        String message;
    }

    private class MyAsyncTask extends AsyncTask<Void, Progress, Boolean> {

        @Override
        protected void onProgressUpdate(Progress... values) {
            Progress progress = values[0];
            mProgressBar.setProgress(progress.progress);
            mProgressBar.setMax(progress.total);
            mTxtLoadingInfo.setText(progress.message);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Progress progress = new Progress();
                if (!mIsCaneled) {
                    progress.progress = 50;
                    progress.message = "同步专题";
                    publishProgress(progress);
                    JsonUtil.updateSubjects(getApplicationContext());
                    progress.progress = 100;
                    progress.message = "完成同步专题";
                    publishProgress(progress);
                }

                JsonUtil.updatePurchaseSubjects(getApplicationContext());

                if (!mIsCaneled) {
                    progress.progress = 50;
                    progress.message = "更新新闻";
                    publishProgress(progress);
                    JsonUtil.updateNews(getApplicationContext());
                    progress.progress = 100;
                    progress.message = "新闻更新完成";
                    publishProgress(progress);
                }

                if (!mIsCaneled) {
                    // update the articles
                    progress.progress = 0;
                    progress.message = "更新法条";
                    publishProgress(progress);
                    String lastUpdateTime = mDbHelper.getLastUpdateArticleTime();
                    int pageIndex = 0;
                    int totalPages = JsonUtil.updateArticles(getApplicationContext(), pageIndex,
                            lastUpdateTime);
                    progress.progress = 1;
                    progress.total = totalPages;
                    while (pageIndex < totalPages && !mIsCaneled) {
                        Log.d("LoginActivity", "pageIndex:" + pageIndex + "totalPages:"
                                + totalPages);
                        pageIndex++;
                        JsonUtil.updateArticles(getApplicationContext(), pageIndex, lastUpdateTime);
                        progress.progress = pageIndex;
                        progress.message = "更新法条";
                        publishProgress(progress);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            if (result) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_SHORT).show();
            }
            if (mCheckBox.isChecked()) {

            }
        }

    }
}
