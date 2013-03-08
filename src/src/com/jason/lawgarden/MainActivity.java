package com.jason.lawgarden;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

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
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
            case R.id.rbtn_law_data:
                LawsFragement lawsFragement = new LawsFragement();
                addLawFragement(0, null, lawsFragement);
                break;
            case R.id.rbtn_law_fav:
                NewsOfLawFragment newsFragment = new NewsOfLawFragment();
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, newsFragment);
                transaction.commit();
                break;
            case R.id.rbtn_law_news:
                break;
            case R.id.rbtn_law_user:
                break;
            }
        }
    };

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

}
