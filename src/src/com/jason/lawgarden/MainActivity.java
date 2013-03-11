package com.jason.lawgarden;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jason.util.JsonUtil;

public class MainActivity extends FragmentActivity {

    private FragmentManager mFragmentManager;

    private RadioGroup mRadioGroup;

    private RadioButton mRbtnLawData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();

        mRadioGroup = (RadioGroup) findViewById(R.id.rgp_bottom);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mRbtnLawData = (RadioButton) findViewById(R.id.rbtn_law_data);
        mRbtnLawData.setChecked(true);
        new MyAsyncTask().execute();
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            popupAllFragmentStack();

            switch (checkedId) {
            case R.id.rbtn_law_data:
                LawsFragement lawsFragement = new LawsFragement();
                addLawFragement(0, null, lawsFragement);
                break;
            case R.id.rbtn_law_fav:
                MyFavoriteFragment favoriteFragment = new MyFavoriteFragment();
                FragmentTransaction favoriteTransaction = mFragmentManager.beginTransaction();
                favoriteTransaction.replace(R.id.fragment_container, favoriteFragment);
                favoriteTransaction.addToBackStack(null);
                favoriteTransaction.commit();
                break;
            case R.id.rbtn_law_news:
                NewsOfLawFragment newsFragment = new NewsOfLawFragment();
                FragmentTransaction newsTransaction = mFragmentManager.beginTransaction();
                newsTransaction.replace(R.id.fragment_container, newsFragment);
                newsTransaction.addToBackStack(null);
                newsTransaction.commit();
                break;
            case R.id.rbtn_law_user:
                UserFragment userFragment = new UserFragment();
                FragmentTransaction userTransaction = mFragmentManager.beginTransaction();
                userTransaction.replace(R.id.fragment_container, userFragment);
                userTransaction.addToBackStack(null);
                userTransaction.commit();
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

    private void addLawFragement(int parentId, String name, LawsFragement fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(LawsFragement.EXTRA_KEY_SUBJECT_ID, parentId);
        bundle.putString(LawsFragement.EXTRA_KEY_SUBJECT_NAME, name);
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

    private class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... params) {
//            try {
//                JsonUtil.login("jason", "123456");
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            return null;
        }
    }
}
