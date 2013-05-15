package com.jason.lawgarden;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;
import com.jason.lawgarden.model.Subject;
import com.jason.util.JsonUtil;

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

    private SearchArticlesAsyncTask mSearchArticlesAsyncTask;

    private FragmentManager mFragmentManager;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

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
        mEditSearch = (EditText) view.findViewById(R.id.edit_search);
        mListLaw = (ListView) view.findViewById(R.id.list_law);
        mListLaw.setAdapter(mAdapter);
        mListLaw.setOnItemClickListener(mOnItemClickListener);
        mListArticle = (ListView) view.findViewById(R.id.list_articles);
        mListArticle.setAdapter(mArticleAdapter);

        txt_no_data = (TextView) view.findViewById(R.id.txt_no_data);
        btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(mOnBtnCancelClickListener);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.rgrp_top);
        ((RadioButton) mRadioGroup.getChildAt(0)).setChecked(true);
        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mEditSearch.setOnEditorActionListener(mOnEditorActionListener);
        mEditSearch.requestFocus();
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
            mArticleListFragment.clearContent();
            if (!mArticleFragement.getView().isShown()) {
                mArticleListFragment.getView().setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSearchArticlesAsyncTask != null) {
            mSearchArticlesAsyncTask.cancel(true);
        }
    }

    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (mProgressDialog == null) {
                initDialog();
            } else {
                mProgressDialog.show();
            }
            if (mSearchArticlesAsyncTask != null) {
                mSearchArticlesAsyncTask.cancel(true);
            }
            // if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            switch (mRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rbtn_subject:
                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(SearchType.LAW);
                mSearchArticlesAsyncTask.execute();
                break;
            case R.id.rbtn_article:
                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(SearchType.ARTICLE_CONTENT);
                mSearchArticlesAsyncTask.execute();
                break;
            case R.id.rbtn_title_text:
                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(SearchType.ARTICLE_TITLE);
                mSearchArticlesAsyncTask.execute();
                break;
            }
            new MyFavoriteAyncTask().execute();
            return true;
            // }
        }
    };

    @Override
    public void onDestroy() {
        if (mSearchArticlesAsyncTask != null) {
            mSearchArticlesAsyncTask.cancel(true);
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

    private enum SearchType {
        LAW, ARTICLE_TITLE, ARTICLE_CONTENT
    }

    private class SearchArticlesAsyncTask extends AsyncTask<Void, Void, Void> {

        private SearchType mType;

        SearchArticlesAsyncTask(SearchType type) {
            mType = type;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(mEditSearch.getText())) {
                switch (mType) {
                case LAW:
                    mSubjects = mDbHelper.searchSubjects(mEditSearch.getText() + "");
                    break;
                case ARTICLE_TITLE:
                    mArticles = mDbHelper.searchArticlesByTitle(mEditSearch.getText() + "");
                    break;
                case ARTICLE_CONTENT:
                    mArticles = mDbHelper.searchArticles(mEditSearch.getText() + "");
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (mType) {
            case LAW:
                mAdapter.notifyDataSetChanged();
                break;
            case ARTICLE_TITLE:
            case ARTICLE_CONTENT:
                mArticleAdapter.notifyDataSetChanged();
                break;
            }
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (TextUtils.isEmpty(mEditSearch.getText())) {
                return;
            }
            if (mProgressDialog == null) {
                initDialog();
            } else {
                mProgressDialog.show();
            }
            if (mSearchArticlesAsyncTask != null) {
                mSearchArticlesAsyncTask.cancel(true);
            }
            txt_no_data.setVisibility(View.GONE);
            ((RadioButton) mRadioGroup.getChildAt(0)).setTextColor(mNormalColor);
            ((RadioButton) mRadioGroup.getChildAt(1)).setTextColor(mNormalColor);
            ((RadioButton) mRadioGroup.getChildAt(2)).setTextColor(mNormalColor);
            switch (checkedId) {
            case R.id.rbtn_subject:
                mListLaw.setVisibility(View.VISIBLE);
                mListArticle.setVisibility(View.GONE);

                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(SearchType.LAW);
                mSearchArticlesAsyncTask.execute();
                ((RadioButton) mRadioGroup.getChildAt(0)).setTextColor(mSelectColor);
                break;
            case R.id.rbtn_article:
                mListLaw.setVisibility(View.GONE);
                mListArticle.setVisibility(View.VISIBLE);
                mListArticle.setOnItemClickListener(mOnArticleItemClickListener);

                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(SearchType.ARTICLE_CONTENT);
                mSearchArticlesAsyncTask.execute();
                ((RadioButton) mRadioGroup.getChildAt(2)).setTextColor(mSelectColor);
                break;
            case R.id.rbtn_title_text:
                mListLaw.setVisibility(View.GONE);
                mListArticle.setVisibility(View.VISIBLE);
                mListArticle.setOnItemClickListener(mOnArticleItemClickListener);
                mSearchArticlesAsyncTask = new SearchArticlesAsyncTask(SearchType.ARTICLE_TITLE);
                mSearchArticlesAsyncTask.execute();
                ((RadioButton) mRadioGroup.getChildAt(1)).setTextColor(mSelectColor);
                break;
            }
        }
    };

    private ProgressDialog mProgressDialog;
    private TextView mTxtLoadingInfo;

    private void initDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.loading_dialog_layout);
        mTxtLoadingInfo = (TextView) mProgressDialog.findViewById(R.id.txt_loading_info);
        mTxtLoadingInfo.setText("正在搜索。。。");
        mProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (mSearchArticlesAsyncTask != null) {
                    mSearchArticlesAsyncTask.cancel(true);
                }
            }
        });
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Subject subject = mSubjects.get(postion);

            if (subject.isNew()) {
                subject.setNew(false);
                mDbHelper.updateSubject(subject);
            }

            if (mArticleListFragment == null) {
                // phone
                addLawsFragment(subject);
            } else if (mArticleListFragment != null) {
                // need show the article in the details pane for tablet
                if (mDbHelper.getSubjectsByParentId(subject.getId()).size() > 0) {
                    addLawsFragment(subject);
                }
                mArticleFragement.getView().setVisibility(View.GONE);
                mArticleListFragment.getView().setVisibility(View.VISIBLE);

                if (mDbHelper.isExistArticlesInSubject(subject.getId())) {
                    mArticleListFragment.updateContent(subject.getId());
                }
            }
        }

        private void addLawsFragment(Subject subject) {
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
            // if (TextUtils.isEmpty(article.getSubjects())) {
            // showBuyDialog();
            // return;
            // }
            // if (!mDbHelper.isArticleAuthorized2(article.getSubjects(),
            // JsonUtil.sUser.getId())) {
            // showBuyDialog();
            // return;
            // }
            if (article.isNew()) {
                article.setNew(false);
                mDbHelper.updateArticles(article);
            }

            if (mArticleListFragment == null) {
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
            } else {
                mArticleListFragment.getView().setVisibility(View.GONE);
                mArticleFragement.getView().setVisibility(View.VISIBLE);
                mArticleFragement.updateContent(article.getId(), article.getTitle());
            }
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
