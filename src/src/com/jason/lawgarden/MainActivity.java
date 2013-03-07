package com.jason.lawgarden;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends FragmentActivity {

    private LawsFragement mLawsFragement;

    private LawDetailsFragement mLawDetailsFragement;

    private RadioGroup mRadioGroup;

    public interface SubjectCallBack {
        void openSubjectByParentId(int parentId, String name);

        void onLastSubjectItemClick(int subjectId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRadioGroup = (RadioGroup) findViewById(R.id.rgp_bottom);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mLawsFragement = new LawsFragement();
        mLawDetailsFragement = new LawDetailsFragement();

        addLawFragement(0, null, mLawsFragement);
        mLawsFragement.setSubjectCallBack(mSubjectCallBack);
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
            case R.id.rbtn_law_data:
                break;
            case R.id.rbtn_law_fav:
                break;
            case R.id.rbtn_law_news:
                break;
            case R.id.rbtn_law_user:
                break;
            }
        }
    };

    private SubjectCallBack mSubjectCallBack = new SubjectCallBack() {

        @Override
        public void openSubjectByParentId(int parentId, String name) {
            LawsFragement fragment = new LawsFragement();
            addLawFragement(parentId, name, fragment);
            fragment.setSubjectCallBack(this);
        }

        @Override
        public void onLastSubjectItemClick(int subjectId) {
            getSupportFragmentManager().popBackStack();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Replace whatever is in the fragment_container view with this
            // fragment, and add the transaction to the back stack so the user
            // can navigate back
            transaction.replace(R.id.fragment_container, mLawDetailsFragement);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    };

    private void addLawFragement(int parentId, String name, LawsFragement fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(LawsFragement.EXTRA_KEY_SUBJECT_ID, parentId);
        bundle.putString(LawsFragement.EXTRA_KEY_SUBJECT_NAME, name);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

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
