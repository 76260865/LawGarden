package com.jason.lawgarden;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.News;

public class NewsDetailsFragment extends Fragment {

    public static final String EXTRA_KEY_NEWS_ID = "extra_key_news_id";

    private News mNews;

    private int mId;

    private DataBaseHelper mDbHelper;

    private TextView mTxtTitle;
    private TextView mTxtTime;
    private TextView txt_from;
    private TextView txt_content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getInt(EXTRA_KEY_NEWS_ID, 0);
        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();

        mNews = mDbHelper.getNewsById(mId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_details_layout, null);
        mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
        mTxtTime = (TextView) view.findViewById(R.id.txt_time);
        txt_from = (TextView) view.findViewById(R.id.txt_from);
        txt_content = (TextView) view.findViewById(R.id.txt_content);

        mTxtTitle.setText(mNews.getTitle());
        mTxtTime.setText(mNews.getCrateTime().toString());
        txt_from.setText(mNews.getFrom());
        txt_content.setText(mNews.getContent());
        return view;
    }
}
