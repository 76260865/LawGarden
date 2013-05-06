package com.jason.lawgarden;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.User;
import com.jason.util.JsonUtil;

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

        new MyAsyncTask().execute();

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

    void showDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance(mRbtnLawData);
        newFragment.show(ft, "dialog");
    }

    public static class MyDialogFragment extends DialogFragment {

        private User mUser;

        private RadioButton mRbtnLawData;

        /**
         * Create a new instance of MyDialogFragment, providing "num" as an
         * argument.
         */
        static MyDialogFragment newInstance(RadioButton mRbtnLawData) {
            MyDialogFragment f = new MyDialogFragment();
            f.mRbtnLawData = f.mRbtnLawData;
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mUser = JsonUtil.sUser;
            View view = inflater.inflate(R.layout.user_info_layout, container, false);
            TextView txtUserName = (TextView) view.findViewById(R.id.txt_user_name);
            TextView txtPurchaseDate = (TextView) view.findViewById(R.id.txt_purchase_date);
            TextView txtOverdueDate = (TextView) view.findViewById(R.id.txt_overdue_date);
            TextView txtAboutUs = (TextView) view.findViewById(R.id.txt_about_us_content);

            txtUserName.setText(getString(R.string.txt_user_name_format_text, mUser.getUserName()));
            // txtPurchaseDate.setText(getString(R.string.txt_purchase_date_format_text,
            // mSimpleDateFormat.format(mUser.getPurchaseDate())));
            // txtOverdueDate.setText(getString(R.string.txt_overdue_date_format_text,
            // mSimpleDateFormat.format(mUser.getOverdueDate())));
            txtAboutUs.setText(mUser.getAboutUs());
            return view;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mRbtnLawData != null) {
                mRbtnLawData.setChecked(true);
            }
        }

    }
}
