package com.jason.lawgarden;

import com.jason.lawgarden.db.DataBaseHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends FragmentActivity {

    private FragmentManager mFragmentManager;

    private RadioGroup mRadioGroup;

    private RadioButton mRbtnLawData;

    private ArticleFragement mArticleFragement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();

        mRadioGroup = (RadioGroup) findViewById(R.id.rgp_bottom);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mRbtnLawData = (RadioButton) findViewById(R.id.rbtn_law_data);
        mRbtnLawData.setChecked(true);

        mArticleFragement = (ArticleFragement) mFragmentManager
                .findFragmentById(R.id.fragment_detail);
        new MyAsyncTask().execute();

    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            popupAllFragmentStack();

            switch (checkedId) {
            case R.id.rbtn_law_data:
                if (mArticleFragement != null) {
                    mArticleFragement.clearContent();
                    mArticleFragement.getView().setVisibility(View.VISIBLE);
                }
                LawsFragment lawsFragement = new LawsFragment();
                addLawFragement(-1, null, lawsFragement);
                // getActionBar().setTitle(R.string.rtbn_law_data_text);
                break;
            case R.id.rbtn_law_fav:
                if (mArticleFragement != null) {
                    mArticleFragement.getView().setVisibility(View.GONE);
                }
                MyFavoriteFragment favoriteFragment = new MyFavoriteFragment();
                FragmentTransaction favoriteTransaction = mFragmentManager.beginTransaction();
                favoriteTransaction.replace(R.id.fragment_container, favoriteFragment);
                favoriteTransaction.addToBackStack(null);
                favoriteTransaction.commit();
                // getActionBar().setTitle(R.string.rbtn_my_favorite_text);
                break;
            case R.id.rbtn_law_news:
                if (mArticleFragement != null) {
                    mArticleFragement.getView().setVisibility(View.GONE);
                }
                NewsOfLawFragment newsFragment = new NewsOfLawFragment();
                FragmentTransaction newsTransaction = mFragmentManager.beginTransaction();
                newsTransaction.replace(R.id.fragment_container, newsFragment);
                newsTransaction.addToBackStack(null);
                newsTransaction.commit();
                // getActionBar().setTitle(R.string.rbtn_law_news_text);
                break;
            case R.id.rbtn_law_user:
                if (mArticleFragement != null) {
                    mArticleFragement.getView().setVisibility(View.GONE);
                }
                UserFragment userFragment = new UserFragment();
                FragmentTransaction userTransaction = mFragmentManager.beginTransaction();
                userTransaction.replace(R.id.fragment_container, userFragment);
                userTransaction.addToBackStack(null);
                userTransaction.commit();
                // getActionBar().setTitle(R.string.rbtn_personal_center_text);
                break;
            }
        }
    };

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

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // try {
            // JsonUtil.login("jason", "123456");
            // } catch (JSONException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            return null;
        }
    }

    @Override
    public void onDestroy() {
        DataBaseHelper.getSingleInstance(this).close();
        super.onDestroy();
    }

}
