package com.jason.lawgarden;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.jason.util.JsonUtil;

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

    private FragmentManager mFragmentManager;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());

        new FavoriteAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_favorite, null);
        mListFavorite = (ListView) view.findViewById(R.id.list_law);
        mListFavorite.setOnItemClickListener(mOnItemClickListener);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.rgrp_top);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        ((RadioButton) mRadioGroup.getChildAt(1)).setChecked(true);

        mImageFavorite = (ImageView) view.findViewById(R.id.img_favorite);
        mImageFavorite.setImageResource(mInEditMode ? R.drawable.usercenter_ok
                : R.drawable.usercenter_edit);
        mImageFavorite.setOnClickListener(mOnClickListener);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mArticleListFragment = (ArticleListFragment) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article_list);
        mArticleFragement = (ArticleFragement) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mArticleListFragment != null) {
            mArticleListFragment.getView().setVisibility(View.GONE);
            if (((RadioButton) mRadioGroup.getChildAt(0)).isChecked()) {
                mArticleFragement.clearContent();
            } else {
                if (mArticleFavoritesList.size() > 0) {
                    Favorite favorite = mArticleFavoritesList.get(0);
                    mArticleFragement.updateContent(favorite.getFavoriteId(),
                            favorite.getTitle());
                }
            }
            mArticleFragement.getView().setVisibility(View.VISIBLE);
        }
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
                                .removeFavoriteByFavoriteIds(new int[] { favorite
                                        .getFavoriteId() });
                        mFavoritesList.remove(favorite);
                    }
                }

                for (int i = mArticleFavoritesList.size() - 1; i >= 0; i--) {
                    Favorite favorite = mArticleFavoritesList.get(i);
                    if (!favorite.isFavorited()) {
                        mDbHelper
                                .removeFavoriteByFavoriteIds(new int[] { favorite
                                        .getFavoriteId() });
                        mArticleFavoritesList.remove(favorite);
                    }
                }
            }
            mInEditMode = !mInEditMode;
            mImageFavorite
                    .setImageResource(mInEditMode ? R.drawable.usercenter_ok
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
            if (mArticleFragement != null && mArticleFavoritesList.size() > 0) {
                Favorite favorite = mArticleFavoritesList.get(0);
                mArticleFragement.updateContent(favorite.getFavoriteId(),
                        favorite.getTitle());
            }
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
    private AlertDialog alert;

    private void showBuyDialog() {
        if (alert == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("1、付费专题！请您到网站支付后查阅！\n 2、付费后，您可在所有客户端查阅专题数据。")
                    .setCancelable(true)
                    .setPositiveButton("购买",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Uri content_url = Uri
                                            .parse("http://www.lawyer1981.com");
                                    intent.setData(content_url);
                                    startActivity(intent);
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton("不购买",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    dialog.cancel();
                                }
                            });
            alert = builder.create();
        }
        alert.show();
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion,
                long id) {
            Favorite favorite = mCurrentType == TYPE_SUBJECTS ? mFavoritesList
                    .get(postion) : mArticleFavoritesList.get(postion);
            if (favorite.getFavoriteType() == 0) {
                if (!mDbHelper.isAuthorized(favorite.getFavoriteId(),
                        JsonUtil.sUser.getId())) {
                    showBuyDialog();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putInt(LawsFragment.EXTRA_KEY_SUBJECT_ID,
                        favorite.getFavoriteId());
                bundle.putString(LawsFragment.EXTRA_KEY_SUBJECT_NAME,
                        favorite.getTitle());
                bundle.putBoolean(LawsFragment.EXTRA_KEY_SUBJECT_IS_FAVORITED,
                        true);

                LawsFragment fragment = new LawsFragment();
                fragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity()
                        .getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            } else {
                if (mArticleListFragment == null) {
                    // TODO:
                    // if (!mDbHelper.isArticleAuthorized2(mDbHelper
                    // .getArticleById(favorite.getFavoriteId())
                    // .getSubjects(), JsonUtil.sUser.getId())) {
                    // showBuyDialog();
                    // return;
                    // }
                    Bundle bundle = new Bundle();
                    bundle.putString(ArticleFragement.EXTRA_KEY_ARTICLE_TITLE,
                            favorite.getTitle());
                    // bundle.putBoolean(LawsFragment.EXTRA_KEY_SUBJECT_IS_FAVORITED,
                    // true);

                    ArticleFragement fragment = new ArticleFragement();

                    fragment.setArguments(bundle);

                    FragmentTransaction transaction = getActivity()
                            .getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                } else {
                    mArticleFragement.updateContent(favorite.getFavoriteId(),
                            favorite.getTitle());
                }
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

            final Favorite favorite = mType == TYPE_SUBJECTS ? mFavoritesList
                    .get(position) : mArticleFavoritesList.get(position);
            TextView txtTitle = (TextView) convertView
                    .findViewById(R.id.txt_law_title);
            final ImageView imgFavorite = (ImageView) convertView
                    .findViewById(R.id.img_favorite);
            txtTitle.setText(favorite.getTitle());
            imgFavorite.setVisibility(mInEditMode ? View.VISIBLE
                    : View.INVISIBLE);
            imgFavorite
                    .setImageResource(favorite.isFavorited() ? R.drawable.list_start_sect
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
