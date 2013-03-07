package com.jason.lawgarden;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LawDetailsFragement extends Fragment {

    private TextView mTxtLawContent;

    public static final String EXTRA_KEY_ARTICLE_CONTENT = "extra_key_article_content";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_details_layout, null);
        mTxtLawContent = (TextView) view.findViewById(R.id.txt_article_content);
        mTxtLawContent.setText(getArguments().getString(EXTRA_KEY_ARTICLE_CONTENT));
        return view;
    }
}
