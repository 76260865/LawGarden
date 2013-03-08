package com.jason.lawgarden;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.News;

public class NewsOfLawFragment extends Fragment {
    private DataBaseHelper mDbHelper;

    private ArrayList<News> mNewsList = new ArrayList<News>();

    private ListView mListLaw;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();
        mNewsList = mDbHelper.getAllNews();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_of_law_layout, null);
        mListLaw = (ListView) view.findViewById(R.id.lst_news);
        mListLaw.setAdapter(new NewsAdapter());
        return view;
    }

    private class NewsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNewsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mNewsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.news_of_law_item_layout, null);
            }
            News news = mNewsList.get(position);

            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
            TextView txtContent = (TextView) convertView.findViewById(R.id.txt_content);
            TextView txtTime = (TextView) convertView.findViewById(R.id.txt_time);
            TextView txtFrom = (TextView) convertView.findViewById(R.id.txt_from);

            txtTitle.setText(news.getTitle());
            txtContent.setText(news.getContent());
            txtTime.setText(sdf.format(news.getCrateTime()));
            txtFrom.setText(news.getFrom());

            return convertView;
        }
    }
}
