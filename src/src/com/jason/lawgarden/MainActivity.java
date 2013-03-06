package com.jason.lawgarden;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class MainActivity extends FragmentActivity {

    private LawsFragement mLawsFragement;

    private LawDetailsFragement mLawDetailsFragement;

    public interface SubjectCallBack {
        void openSubjectByParentId(int parentId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLawsFragement = new LawsFragement();
        mLawDetailsFragement = new LawDetailsFragement();

        addLawFragement(0, mLawsFragement);
        mLawsFragement.setSubjectCallBack(mSubjectCallBack);
    }

    public void onTxtLawDataClick(View view) {
    }

    private SubjectCallBack mSubjectCallBack = new SubjectCallBack() {

        @Override
        public void openSubjectByParentId(int parentId) {
            LawsFragement fragment = new LawsFragement();
            addLawFragement(parentId, fragment);
            fragment.setSubjectCallBack(this);
        }
    };

    private void addLawFragement(int parentId, LawsFragement fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(LawsFragement.EXTRA_KEY_SUBJECT_ID, parentId);
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

    public void onTxtLawNewsClick(View view) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack so the user can navigate
        // back
        transaction.replace(R.id.fragment_container, mLawDetailsFragement);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
