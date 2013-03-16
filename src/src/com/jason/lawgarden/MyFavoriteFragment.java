package com.jason.lawgarden;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Favorite;

public class MyFavoriteFragment extends Fragment {
    private DataBaseHelper mDbHelper;

    private ArrayList<Favorite> mFavoritesList = new ArrayList<Favorite>();

    private ListView mListFavorite;

    private boolean mInEditMode;

    private FavoriteAdapter mFavoriteAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();
        mFavoritesList = mDbHelper.getAllFavorites();
        setHasOptionsMenu(true);
        mFavoriteAdapter = new FavoriteAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_favorite, null);
        mListFavorite = (ListView) view.findViewById(R.id.list_law);
        mListFavorite.setAdapter(mFavoriteAdapter);
        mListFavorite.setOnItemClickListener(mOnItemClickListener);

        return view;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Favorite favorite = mFavoritesList.get(postion);
            if (favorite.getFavoriteType() == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt(LawsFragement.EXTRA_KEY_SUBJECT_ID, favorite.getFavoriteId());
                bundle.putString(LawsFragement.EXTRA_KEY_SUBJECT_NAME, favorite.getTitle());
                bundle.putBoolean(LawsFragement.EXTRA_KEY_SUBJECT_IS_FAVORITED, true);

                LawsFragement fragment = new LawsFragement();
                fragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();

                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            } else {
                Bundle bundle = new Bundle();
                bundle.putInt(LawDetailsFragement.EXTRA_KEY_ARTICLE_ID, favorite.getFavoriteId());
                bundle.putBoolean(LawsFragement.EXTRA_KEY_SUBJECT_IS_FAVORITED, true);

                LawDetailsFragement fragment = new LawDetailsFragement();

                fragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();

                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mInEditMode) {
            // delete the unfavorited items:
            for (int i = mFavoritesList.size() - 1; i >= 0; i--) {
                Favorite favorite = mFavoritesList.get(i);
                if (!favorite.isFavorited()) {
                    mDbHelper.removeFavoriteByFavoriteIds(new int[] { favorite.getFavoriteId() });
                    mFavoritesList.remove(favorite);
                }
            }
        }
        mInEditMode = !mInEditMode;
        item.setIcon(mInEditMode ? R.drawable.usercenter_ok : R.drawable.usercenter_edit);
        mFavoriteAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.getItem(1)
                .setIcon(mInEditMode ? R.drawable.usercenter_ok : R.drawable.usercenter_edit);
        super.onPrepareOptionsMenu(menu);
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

            final Favorite favorite = mFavoritesList.get(position);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_law_title);
            final ImageView imgFavorite = (ImageView) convertView.findViewById(R.id.img_favorite);
            txtTitle.setText(favorite.getTitle());
            imgFavorite.setVisibility(mInEditMode ? View.VISIBLE : View.INVISIBLE);
            imgFavorite.setImageResource(favorite.isFavorited() ? R.drawable.list_start_sect
                    : R.drawable.list_start);
            imgFavorite.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    favorite.setFavorited(!favorite.isFavorited());
                    imgFavorite.setImageResource(favorite.isFavorited() ? R.drawable.list_start_sect
                            : R.drawable.list_start);
                }
            });

            return convertView;
        }

    }
}
