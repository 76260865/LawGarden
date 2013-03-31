package com.jason.lawgarden;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;
import com.jason.lawgarden.model.Subject;

public class SearchFragment extends Fragment {

    private DataBaseHelper mDbHelper;

    private ArrayList<Subject> mSubjects = new ArrayList<Subject>();

    private ArrayList<Article> mArticles = new ArrayList<Article>();

    private BaseAdapter mAdapter;

    private BaseAdapter mArticleAdapter;

    private RadioGroup mRadioGroup;

    private ListView mListLaw;

    private ListView mListArticle;

    private EditText mEditSearch;

    private Button btn_cancel;

    private int mNormalColor;

    private int mSelectColor;

    private TextView txt_no_data;

    private SearchLawsAsyncTask mSearchLawsAsyncTask;

    private SearchArticlesAsyncTask mSearchArticlesAsyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());

        mAdapter = new LawsAdapter();
        mArticleAdapter = new ArticlesAdapter();

        mNormalColor = getActivity().getResources().getColor(R.color.rbtn_text_normal_color);
        mSelectColor = getActivity().getResources().getColor(R.color.rbtn_text_select_color);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_list_layout, container, false);
        mListLaw = (ListView) view.findViewById(R.id.list_law);
        mListLaw.setAdapter(mAdapter);
        mListArticle = (ListView) view.findViewById(R.id.list_articles);
        mListArticle.setAdapter(mArticleAdapter);

        txt_no_data = (TextView) view.findViewById(R.id.txt_no_data);
        btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(mOnBtnCancelClickListener);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.rgrp_top);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        ((RadioButton) mRadioGroup.getChildAt(0)).setChecked(true);

        mEditSearch = (EditText) view.findViewById(R.id.edit_search);
        mEditSearch.setOnEditorActionListener(mOnEditorActionListener);
        mEditSearch.requestFocus();
        return view;
    }

    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                switch (mRadioGroup.getCheckedRadioButtonId()) {
                case R.id.rbtn_subject:
                    if (mSearchLawsAsyncTask != null) {
                        mSearchLawsAsyncTask.cancel(true);
                    }
                    mSearchLawsAsyncTask = new SearchLawsAsyncTask();
                    mSearchLawsAsyncTask.execute();
                    break;
                case R.id.rbtn_article:
                    if (mSearchArticlesAsyncTask != null) {
                        mSearchArticlesAsyncTask.cancel(true);
                    }
                    mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(false);
                    mSearchArticlesAsyncTask.execute();
                    break;
                case R.id.rbtn_title_text:
                    if (mSearchArticlesAsyncTask != null) {
                        mSearchArticlesAsyncTask.cancel(true);
                    }
                    mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(true);
                    mSearchArticlesAsyncTask.execute();
                    break;
                }
                new MyFavoriteAyncTask().execute();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onDestroy() {
        if (mSearchArticlesAsyncTask != null) {
            mSearchArticlesAsyncTask.cancel(true);
        }
        if (mSearchLawsAsyncTask != null) {
            mSearchLawsAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private OnClickListener mOnBtnCancelClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditSearch.getWindowToken(), 0);
            getActivity().getSupportFragmentManager().popBackStack();
        }
    };

    private class SearchLawsAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(mEditSearch.getText())) {
                mSubjects = mDbHelper.searchSubjects(mEditSearch.getText() + "");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.notifyDataSetChanged();
        }

    }

    private class SearchArticlesAsyncTask extends AsyncTask<Void, Void, Void> {

        private boolean mIsTitle = false;

        SearchArticlesAsyncTask(boolean isTitle) {
            mIsTitle = isTitle;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(mEditSearch.getText())) {
                if (mIsTitle) {
                    mArticles = mDbHelper.searchArticlesByTitle(mEditSearch.getText() + "");
                } else {
                    mArticles = mDbHelper.searchArticles(mEditSearch.getText() + "");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mArticleAdapter.notifyDataSetChanged();
        }
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            txt_no_data.setVisibility(View.GONE);
            ((RadioButton) mRadioGroup.getChildAt(0)).setTextColor(mNormalColor);
            ((RadioButton) mRadioGroup.getChildAt(1)).setTextColor(mNormalColor);
            ((RadioButton) mRadioGroup.getChildAt(2)).setTextColor(mNormalColor);
            switch (checkedId) {
            case R.id.rbtn_subject:
                mListLaw.setVisibility(View.VISIBLE);
                mListArticle.setVisibility(View.GONE);
                mListLaw.setOnItemClickListener(mOnItemClickListener);

                if (mSearchLawsAsyncTask != null) {
                    mSearchLawsAsyncTask.cancel(true);
                }
                mSearchLawsAsyncTask = new SearchLawsAsyncTask();
                mSearchLawsAsyncTask.execute();
                ((RadioButton) mRadioGroup.getChildAt(0)).setTextColor(mSelectColor);
                break;
            case R.id.rbtn_article:
                mListLaw.setVisibility(View.GONE);
                mListArticle.setVisibility(View.VISIBLE);
                mListArticle.setOnItemClickListener(mOnArticleItemClickListener);

                if (mSearchArticlesAsyncTask != null) {
                    mSearchArticlesAsyncTask.cancel(true);
                }
                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(false);
                mSearchArticlesAsyncTask.execute();
                ((RadioButton) mRadioGroup.getChildAt(2)).setTextColor(mSelectColor);
                break;
            case R.id.rbtn_title_text:
                mListLaw.setVisibility(View.GONE);
                mListArticle.setVisibility(View.VISIBLE);
                mListArticle.setOnItemClickListener(mOnArticleItemClickListener);
                if (mSearchArticlesAsyncTask != null) {
                    mSearchArticlesAsyncTask.cancel(true);
                }
                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(true);
                mSearchArticlesAsyncTask.execute();
                ((RadioButton) mRadioGroup.getChildAt(1)).setTextColor(mSelectColor);
                break;
            }
        }
    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Subject subject = mSubjects.get(postion);

            if (subject.isNew()) {
                subject.setNew(false);
                mDbHelper.updateSubject(subject);
            }

            LawsFragment fragment = new LawsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt(LawsFragment.EXTRA_KEY_SUBJECT_ID, subject.getId());
            bundle.putString(LawsFragment.EXTRA_KEY_SUBJECT_NAME, subject.getName());
            bundle.putBoolean(LawsFragment.EXTRA_KEY_SUBJECT_IS_FAVORITED, subject.isFavorited());
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();

            // Replace whatever is in the fragment_container view with this
            // fragment, and add the transaction to the back stack so the
            // user can navigate back
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    };

    private OnItemClickListener mOnArticleItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Article article = mArticles.get(postion);
            if (article.isNew()) {
                article.setNew(false);
                mDbHelper.updateArticles(article);
            }

            Bundle bundle = new Bundle();
            bundle.putString(ArticleFragement.EXTRA_KEY_ARTICLE_TITLE, article.getTitle());
            ArticleFragement fragment = new ArticleFragement();
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    };

    private class LawsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSubjects.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.law_list_item_layout, null);
            }
            final Subject subject = mSubjects.get(position);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_law_title);
            ImageView imgNew = (ImageView) convertView.findViewById(R.id.img_new);
            final ImageView imgFavorite = (ImageView) convertView.findViewById(R.id.img_favorite);
            if (subject.isNew()) {
                imgNew.setImageResource(R.drawable.news);
            }
            imgFavorite.setImageResource(subject.isFavorited() ? R.drawable.list_start_sect
                    : R.drawable.list_start);

            imgFavorite.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (subject.isFavorited()) {
                        mDbHelper.removeFavoriteByFavoriteIds(new int[] { subject.getId() });
                        subject.setFavorited(false);
                    } else {
                        Favorite favorite = new Favorite();
                        favorite.setFavoriteId(subject.getId());
                        favorite.setFavoriteType(0);
                        favorite.setTitle(subject.getName());

                        mDbHelper.addFavorite(favorite);
                        subject.setFavorited(true);
                    }
                    imgFavorite.setImageResource(subject.isFavorited() ? R.drawable.list_start_sect
                            : R.drawable.list_start);
                }
            });

            txtTitle.setText(subject.getName());
            return convertView;
        }
    }

    private class ArticlesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArticles.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.law_list_item_layout, null);
            }
            final Article article = mArticles.get(position);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_law_title);
            ImageView imgNew = (ImageView) convertView.findViewById(R.id.img_new);
            final ImageView imgFavorite = (ImageView) convertView.findViewById(R.id.img_favorite);
            if (article.isNew()) {
                imgNew.setImageResource(R.drawable.news);
            } else {
                imgNew.setImageBitmap(null);
            }
            imgFavorite.setImageResource(article.isFavorite() ? R.drawable.list_start_sect
                    : R.drawable.list_start);

            imgFavorite.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (article.isFavorite()) {
                        mDbHelper.removeFavoriteByFavoriteIds(new int[] { article.getId() });
                        article.setFavorite(false);
                    } else {
                        Favorite favorite = new Favorite();
                        favorite.setFavoriteId(article.getId());
                        favorite.setFavoriteType(1);
                        favorite.setTitle(article.getTitle());

                        mDbHelper.addFavorite(favorite);
                        article.setFavorite(true);
                    }
                    imgFavorite.setImageResource(article.isFavorite() ? R.drawable.list_start_sect
                            : R.drawable.list_start);
                }
            });

            txtTitle.setText(article.getTitle());
            return convertView;
        }
    }

    private class MyFavoriteAyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (((RadioButton) mRadioGroup.getChildAt(0)).isChecked()) {
                for (Article article : mArticles) {
                    boolean ret = mDbHelper.isFavorited(article.getId());
                    article.setFavorite(ret);
                }
            } else {
                for (Subject subject : mSubjects) {
                    boolean ret = mDbHelper.isFavorited(subject.getId());
                    subject.setFavorited(ret);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (((RadioButton) mRadioGroup.getChildAt(0)).isChecked()) {
                mAdapter.notifyDataSetChanged();
            } else {
                mArticleAdapter.notifyDataSetChanged();
            }
        }
    }
}
