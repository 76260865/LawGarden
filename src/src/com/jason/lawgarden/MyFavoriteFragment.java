package com.jason.lawgarden;

import java.util.ArrayList;

import android.os.AsyncTask;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Favorite;

public class MyFavoriteFragment extends Fragment {
    private DataBaseHelper mDbHelper;

    private ArrayList<Favorite> mFavoritesList = new ArrayList<Favorite>();

    private ArrayList<Favorite> mArticleFavoritesList = new ArrayList<Favorite>();

    private ListView mListFavorite;

    private boolean mInEditMode;

    private FavoriteAdapter mFavoriteAdapter;

    private RadioGroup mRadioGroup;

    private ImageView mImageFavorite;

    private static final int TYPE_SUBJECTS = 0;

    private static final int TYPE_ARTICLE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());

        new FavoriteAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_favorite, null);
        mListFavorite = (ListView) view.findViewById(R.id.list_law);
        mListFavorite.setOnItemClickListener(mOnItemClickListener);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.rgrp_top);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        ((RadioButton) mRadioGroup.getChildAt(0)).setChecked(true);

        mImageFavorite = (ImageView) view.findViewById(R.id.img_favorite);
        mImageFavorite.setImageResource(mInEditMode ? R.drawable.usercenter_ok
                : R.drawable.usercenter_edit);
        mImageFavorite.setOnClickListener(mOnClickListener);

        return view;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mInEditMode) {
                // delete the unfavorited items:
                for (int i = mFavoritesList.size() - 1; i >= 0; i--) {
                    Favorite favorite = mFavoritesList.get(i);
                    if (!favorite.isFavorited()) {
                        mDbHelper
                                .removeFavoriteByFavoriteIds(new int[] { favorite.getFavoriteId() });
                        mFavoritesList.remove(favorite);
                    }
                }

                for (int i = mArticleFavoritesList.size() - 1; i >= 0; i--) {
                    Favorite favorite = mArticleFavoritesList.get(i);
                    if (!favorite.isFavorited()) {
                        mDbHelper
                                .removeFavoriteByFavoriteIds(new int[] { favorite.getFavoriteId() });
                        mArticleFavoritesList.remove(favorite);
                    }
                }
            }
            mInEditMode = !mInEditMode;
            mImageFavorite.setImageResource(mInEditMode ? R.drawable.usercenter_ok
                    : R.drawable.usercenter_edit);
            mFavoriteAdapter.notifyDataSetChanged();
        }
    };

    private class FavoriteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<Favorite> favorites = mDbHelper.getAllFavorites();
            for (Favorite favorite : favorites) {
                if (favorite.getFavoriteType() == 0) {
                    mFavoritesList.add(favorite);
                } else {
                    mArticleFavoritesList.add(favorite);
                }
            }
            favorites.clear();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mFavoriteAdapter.notifyDataSetChanged();
        }
    }

    private int mCurrentType = 0;
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
            case R.id.rbtn_subject:
                mFavoriteAdapter = new FavoriteAdapter(TYPE_SUBJECTS);
                mListFavorite.setAdapter(mFavoriteAdapter);
                mCurrentType = TYPE_SUBJECTS;
                break;
            case R.id.rbtn_article:
                mFavoriteAdapter = new FavoriteAdapter(TYPE_ARTICLE);
                mListFavorite.setAdapter(mFavoriteAdapter);
                mCurrentType = TYPE_ARTICLE;
                break;
            }
        }
    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Favorite favorite = mCurrentType == TYPE_SUBJECTS ? mFavoritesList.get(postion)
                    : mArticleFavoritesList.get(postion);
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
                bundle.putInt(ArticleFragement.EXTRA_KEY_ARTICLE_ID, favorite.getFavoriteId());
                bundle.putBoolean(LawsFragement.EXTRA_KEY_SUBJECT_IS_FAVORITED, true);

                ArticleFragement fragment = new ArticleFragement();

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

    private class FavoriteAdapter extends BaseAdapter {

        private int mType;

        public FavoriteAdapter(int type) {
            mType = type;
        }

        @Override
        public int getCount() {
            if (mType == TYPE_SUBJECTS) {
                return mFavoritesList.size();
            } else {
                return mArticleFavoritesList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (mType == TYPE_SUBJECTS) {
                return mFavoritesList.get(position);
            } else {
                return mArticleFavoritesList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.my_favorites_item_layout, null);
            }

            final Favorite favorite = mType == TYPE_SUBJECTS ? mFavoritesList.get(position)
                    : mArticleFavoritesList.get(position);
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
