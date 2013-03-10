package com.jason.lawgarden;

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
import com.jason.lawgarden.model.Favorite;

public class MyFavoriteFragment extends Fragment {
    private DataBaseHelper mDbHelper;

    private ArrayList<Favorite> mFavoritesList = new ArrayList<Favorite>();

    private ListView mListFavorite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();
        mFavoritesList = mDbHelper.getAllFavorites();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_favorite, null);
        mListFavorite = (ListView) view.findViewById(R.id.list_law);
        mListFavorite.setAdapter(new FavoriteAdapter());

        return view;
    }

    private class FavoriteAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFavoritesList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFavoritesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mFavoritesList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.my_favorites_item_layout, null);
            }

            Favorite favorite = mFavoritesList.get(position);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_law_title);
            txtTitle.setText(favorite.getTitle());

            return convertView;
        }

    }
}
