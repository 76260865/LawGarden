package com.jason.lawgarden;

import java.util.ArrayList;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.News;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class MyFavoriteFragment extends Fragment {
    private DataBaseHelper mDbHelper;

    private ArrayList<News> mNewsList = new ArrayList<News>();

    private ListView mListFavorite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_list_layout, null);
        mListFavorite = (ListView) view.findViewById(R.id.list_law);

        return view;
    }

    private class FavoriteAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
