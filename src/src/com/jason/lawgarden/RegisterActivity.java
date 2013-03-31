package com.jason.lawgarden;

import org.json.JSONException;

import com.jason.util.JsonUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";

    private EditText mEditUserName;
    private EditText mEditPassword;
    private EditText mEditPasswordConfirm;
    private EditText mEditUserPhone;

    private ProgressDialog mProgressDialog;
    private String userName;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        mEditUserName = (EditText) findViewById(R.id.edit_user_name);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
        mEditPasswordConfirm = (EditText) findViewById(R.id.edit_passwrod_confirm);
        mEditUserPhone = (EditText) findViewById(R.id.edit_user_phone);
    }

    public void onBtnRegisterClick(View view) {
        if (TextUtils.isEmpty(mEditUserName.getText())
                || TextUtils.isEmpty(mEditPassword.getText())
                || TextUtils.isEmpty(mEditPasswordConfirm.getText())) {
            Toast.makeText(getApplicationContext(), "请输入相关信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mEditPassword.getText().toString().equals(mEditPasswordConfirm.getText().toString())) {
            Toast.makeText(getApplicationContext(), "两次输入密码不同", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        userName = mEditUserName.getText().toString();
        password = mEditPassword.getText().toString();

        new MyAsyncTask().execute();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private String[] message = new String[1];

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                return JsonUtil.register(userName, password, message);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), message[0], Toast.LENGTH_SHORT).show();
            if (result) {
                // Intent intent = new Intent(RegisterActivity.this,
                // LoginActivity.class);
                // startActivity(intent);
                finish();
            }
        }

    }
}
