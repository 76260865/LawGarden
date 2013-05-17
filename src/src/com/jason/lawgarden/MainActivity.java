package com.jason.lawgarden;

import java.text.ParseException;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.util.JsonUtil;
import com.jason.util.NetworkUtil;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private FragmentManager mFragmentManager;

    private RadioGroup mRadioGroup;

    private RadioButton mRbtnLawData;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

    private DataBaseHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();

        mArticleListFragment = (ArticleListFragment) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article_list);
        mArticleFragement = (ArticleFragement) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article);

        mRadioGroup = (RadioGroup) findViewById(R.id.rgp_bottom);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mRbtnLawData = (RadioButton) findViewById(R.id.rbtn_law_data);
        mRbtnLawData.setChecked(true);
        mDbHelper = DataBaseHelper.getSingleInstance(getApplicationContext());

        if (NetworkUtil.isNetworkConnected(getApplicationContext())) {
            new UpdateTask().execute();
        }
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            popupAllFragmentStack();

            switch (checkedId) {
            case R.id.rbtn_law_data:
                if (mArticleListFragment != null) {
                    Log.d("xxxxx", "mArticleListFragment!=null");
                    mArticleFragement.clearContent();
                    mArticleFragement.getView().setVisibility(View.GONE);
                    mArticleListFragment.getView().setVisibility(View.VISIBLE);
                }
                LawsFragment lawsFragement = new LawsFragment();
                addLawFragement(-1, null, lawsFragement);
                break;
            case R.id.rbtn_law_fav:
                if (mArticleFragement != null) {
                    mArticleListFragment.getView().setVisibility(View.GONE);
                    mArticleFragement.clearContent();
                    mArticleFragement.getView().setVisibility(View.VISIBLE);
                }
                MyFavoriteFragment favoriteFragment = new MyFavoriteFragment();
                FragmentTransaction favoriteTransaction = mFragmentManager.beginTransaction();
                favoriteTransaction.replace(R.id.fragment_container, favoriteFragment);
                favoriteTransaction.addToBackStack(null);
                favoriteTransaction.commit();
                break;
            case R.id.rbtn_law_news:
                if (mArticleFragement != null) {
                    mArticleListFragment.getView().setVisibility(View.GONE);
                    mArticleFragement.getView().setVisibility(View.GONE);
                }
                NewsOfLawFragment newsFragment = new NewsOfLawFragment();
                FragmentTransaction newsTransaction = mFragmentManager.beginTransaction();
                newsTransaction.replace(R.id.fragment_container, newsFragment);
                newsTransaction.addToBackStack(null);
                newsTransaction.commit();
                break;
            case R.id.rbtn_law_user:
                if (mArticleFragement != null) {
                    mArticleListFragment.getView().setVisibility(View.GONE);
                    mArticleFragement.getView().setVisibility(View.GONE);
                    // showDialog();
                }
                UserFragment userFragment = new UserFragment();
                FragmentTransaction userTransaction = mFragmentManager.beginTransaction();
                userTransaction.replace(R.id.fragment_container, userFragment);
                userTransaction.addToBackStack(null);
                userTransaction.commit();
                break;
            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mArticleFragement != null
                && mArticleFragement.getView().getVisibility() == View.VISIBLE) {
            mArticleFragement.clearLargeText();
        }
    }

    private void popupAllFragmentStack() {
        for (int i = 0; i < mFragmentManager.getBackStackEntryCount(); i++) {
            mFragmentManager.popBackStack();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 1) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    private void addLawFragement(int parentId, String name, LawsFragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(LawsFragment.EXTRA_KEY_SUBJECT_ID, parentId);
        bundle.putString(LawsFragment.EXTRA_KEY_SUBJECT_NAME, name);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack so the user can navigate
        // back
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onDestroy() {
        DataBaseHelper.getSingleInstance(this).close();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private class UpdateTask extends AsyncTask<Void, Void, Boolean> {

        private boolean mHasUpdateData = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            // publishProgress();
            try {
                JsonUtil.updateUserSubjects(getApplicationContext());
                mHasUpdateData = JsonUtil.CheckAllUpdates(getApplicationContext());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            return mHasUpdateData;
        }

        @Override
        protected void onPostExecute(Boolean result) {
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
            }
        }
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
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            if (result) {
                mRbtnLawData.setChecked(true);
            }
        }

    }

    class Progress {
        int progress;
        int total = 100;
        String message;
    }

    private ProgressDialog mProgressDialog;
    private boolean mIsCaneled = false;
    private TextView mTxtLoadingInfo;
    private Button mBtnOk;
    private Button mBtnCancel;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressLogin;
    private MyAsyncTask mMyAsyncTask;

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
                } else {
                    mMyAsyncTask.cancel(true);
                    mProgressDialog.dismiss();
                }
                Log.d("LoginActivity", "cancel");
            }
        });
    }
}
