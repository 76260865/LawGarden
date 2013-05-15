package com.jason.lawgarden;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jason.lawgarden.db.DataBaseHelper;

public class MainActivity extends FragmentActivity {

    private FragmentManager mFragmentManager;

    private RadioGroup mRadioGroup;

    private RadioButton mRbtnLawData;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

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
        super.onDestroy();
    }
}
