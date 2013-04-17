package com.jason.lawgarden;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;
import com.jason.lawgarden.model.Subject;

public class LawsFragment extends Fragment {
    // private static final String TAG = "LawsFragement";

    public static final String EXTRA_KEY_SUBJECT_ID = "extra_subject_id";

    public static final String EXTRA_KEY_SUBJECT_NAME = "extra_subject_name";

    public static final String EXTRA_KEY_SUBJECT_IS_FAVORITED = "extra_subject_is_favorited";

    private int mSubjectId;

    private DataBaseHelper mDbHelper;

    private ArrayList<Subject> mSubjects = new ArrayList<Subject>();

    private ArrayList<Article> mArticles = new ArrayList<Article>();

    private View mViewSubjectTitle;

    private TextView mTxtSubjectName;

    private String mSubjectName;

    private boolean mIsFavorited;

    private boolean mIsDetails;

    private BaseAdapter mAdapter;

    private BaseAdapter mArticleAdapter;

    private RadioGroup mRadioGroup;

    private ListView mListLaw;

    private ListView mListArticle;

    private ImageView mImageFavorite;

    private EditText mEditSearch;

    private int mNormalColor;

    private int mSelectColor;

    private TextView txt_no_data;

    private boolean isDuralPane = false;

    private ArticleFragement mArticleFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());

        Bundle bundle = getArguments();
        mSubjectId = bundle.getInt(EXTRA_KEY_SUBJECT_ID, -1);
        mSubjectName = bundle.getString(EXTRA_KEY_SUBJECT_NAME);
        mIsFavorited = bundle.getBoolean(EXTRA_KEY_SUBJECT_IS_FAVORITED);

        // mSubjects = mSubjectId == -1 ?
        // mDbHelper.getSubjectsByUserId(JsonUtil.sUser.getId())
        // : mDbHelper.getSubjectsByParentId(mSubjectId);
        // if (mSubjects.size() == 0) {
        // // the last subject, need load the articles
        // // mSubjectCallBack.onLastSubjectItemClick(mSubjectId);
        // mArticles = mDbHelper.getArticlesBySubjectId(mSubjectId);
        // mIsDetails = true;
        // }

        mAdapter = new LawsAdapter();
        mArticleAdapter = new ArticlesAdapter();

        mNormalColor = getActivity().getResources().getColor(R.color.rbtn_text_normal_color);
        mSelectColor = getActivity().getResources().getColor(R.color.rbtn_text_select_color);
    }

    @Override
    public void onResume() {
        super.onResume();
        new MyFavoriteAyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_list_layout, container, false);
        mArticleFragment = (ArticleFragement) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragment_detail);
        View divider = view.findViewById(R.id.divider);
        mListLaw = (ListView) view.findViewById(R.id.list_law);
        mListLaw.setAdapter(mAdapter);
        mListArticle = (ListView) view.findViewById(R.id.list_articles);
        mListArticle.setAdapter(mArticleAdapter);

        mViewSubjectTitle = view.findViewById(R.id.linear_subject);
        mTxtSubjectName = (TextView) view.findViewById(R.id.txt_subject_title);
        txt_no_data = (TextView) view.findViewById(R.id.txt_no_data);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.rgrp_top);
        mImageFavorite = (ImageView) view.findViewById(R.id.img_subject_favorite);
        mImageFavorite.setOnClickListener(mOnClickListener);

        if (mSubjectId > 0) {
            mViewSubjectTitle.setVisibility(View.VISIBLE);
            mTxtSubjectName.setText(mSubjectName);
            divider.setVisibility(View.VISIBLE);
        } else {
            mRadioGroup.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        mImageFavorite.setImageResource(mIsFavorited ? R.drawable.list_start_sect
                : R.drawable.list_start);

        mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        ((RadioButton) mRadioGroup.getChildAt(0)).setChecked(true);

        mEditSearch = (EditText) view.findViewById(R.id.edit_search);
        mEditSearch.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    SearchFragment fragment = new SearchFragment();

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                            .beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
            }
        });
        return view;
    }

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            txt_no_data.setVisibility(View.GONE);
            ((RadioButton) mRadioGroup.getChildAt(0)).setTextColor(mNormalColor);
            ((RadioButton) mRadioGroup.getChildAt(1)).setTextColor(mNormalColor);
            switch (checkedId) {
            case R.id.rbtn_subject:
                mListLaw.setVisibility(View.VISIBLE);
                mListArticle.setVisibility(View.GONE);
                mListLaw.setOnItemClickListener(mOnItemClickListener);

                mIsDetails = false;
                new SubjectsAsyncTask().execute();
                ((RadioButton) mRadioGroup.getChildAt(0)).setTextColor(mSelectColor);
                break;
            case R.id.rbtn_article:
                mListLaw.setVisibility(View.GONE);
                mListArticle.setVisibility(View.VISIBLE);
                mListArticle.setOnItemClickListener(mOnArticleItemClickListener);

                new ArticlesAsyncTask().execute();
                ((RadioButton) mRadioGroup.getChildAt(1)).setTextColor(mSelectColor);
                mIsDetails = true;
                break;
            }
        }
    };

    private class SubjectsAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO: filter by userId
            // mSubjects = mSubjectId == -1 ?
            // mDbHelper.getSubjectsByUserId(JsonUtil.sUser.getId())
            // : mDbHelper.getSubjectsByParentId(mSubjectId);
            mSubjects = mSubjectId == -1 ? mDbHelper.getSubjectsByParentId(0) : mDbHelper
                    .getSubjectsByParentId(mSubjectId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getCount() == 0 && !mIsDetails) {
                txt_no_data.setVisibility(View.VISIBLE);
                mRadioGroup.getChildAt(0).setEnabled(false);
                mRadioGroup.check(R.id.rbtn_article);
                return;
            }
            txt_no_data.setVisibility(View.GONE);
            new MyFavoriteAyncTask().execute();
        }
    }

    private class ArticlesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mArticles = mDbHelper.getArticlesBySubjectId(mSubjectId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mArticleAdapter.notifyDataSetChanged();
            if (mAdapter.getCount() == 0 && mArticleAdapter.getCount() == 0 && mIsDetails) {
                mRadioGroup.setVisibility(View.GONE);
                txt_no_data.setVisibility(View.VISIBLE);
                txt_no_data.setText("��ר��ͷ�����Ϣ!");
                return;
            } else if (mArticleAdapter.getCount() == 0 && mIsDetails) {
                txt_no_data.setVisibility(View.VISIBLE);
                txt_no_data.setText("�޷�����Ϣ!");
            }
            // new MyFavoriteAyncTask().execute();
        }
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Subject subject = mSubjects.get(postion);

            if (subject.isNew()) {
                subject.setNew(false);
                mDbHelper.updateSubject(subject);
            }
            // TODO: check if it is buyed, toast a message if not

            LawsFragment fragment = new LawsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt(LawsFragment.EXTRA_KEY_SUBJECT_ID, subject.getId());
            bundle.putString(LawsFragment.EXTRA_KEY_SUBJECT_NAME, subject.getName());
            bundle.putBoolean(EXTRA_KEY_SUBJECT_IS_FAVORITED, subject.isFavorited());
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

            if (mArticleFragment != null) {
                // Tablet:
                mArticleFragment.updateContent(article.getId(), article.getTitle());
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putInt(ArticleFragement.EXTRA_KEY_ARTICLE_ID, article.getId());
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

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsFavorited) {
                mDbHelper.removeFavoriteByFavoriteIds(new int[] { mSubjectId });
                mIsFavorited = false;
            } else {
                Favorite favorite = new Favorite();
                favorite.setFavoriteId(mSubjectId);
                favorite.setFavoriteType(0);
                favorite.setTitle(mSubjectName);

                mDbHelper.addFavorite(favorite);
                mIsFavorited = true;
            }
            mImageFavorite.setImageResource(mIsFavorited ? R.drawable.list_start_sect
                    : R.drawable.list_start);
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

            // new FavoriteAyncTask(imgFavorite, subject.getId(),
            // position).execute();
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
                        R.layout.article_list_item_layout, null);
            }
            final Article article = mArticles.get(position);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_law_title);
            TextView txtContent = (TextView) convertView.findViewById(R.id.txt_article_content);
            ImageView imgNew = (ImageView) convertView.findViewById(R.id.img_new);
            final ImageView imgFavorite = (ImageView) convertView.findViewById(R.id.img_favorite);
            imgFavorite.setVisibility(View.GONE);
            if (article.isNew()) {
                imgNew.setImageResource(R.drawable.news);
            } else {
                imgNew.setImageBitmap(null);
            }
            // imgFavorite.setImageResource(article.isFavorite() ?
            // R.drawable.list_start_sect
            // : R.drawable.list_start);
            //
            // imgFavorite.setOnClickListener(new OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            // if (article.isFavorite()) {
            // mDbHelper.removeFavoriteByFavoriteIds(new int[] { article.getId()
            // });
            // article.setFavorite(false);
            // } else {
            // Favorite favorite = new Favorite();
            // favorite.setFavoriteId(article.getId());
            // favorite.setFavoriteType(1);
            // favorite.setTitle(article.getTitle());
            //
            // mDbHelper.addFavorite(favorite);
            // article.setFavorite(true);
            // }
            // imgFavorite.setImageResource(article.isFavorite() ?
            // R.drawable.list_start_sect
            // : R.drawable.list_start);
            // }
            // });

            txtTitle.setText(article.getTitle());
            txtContent.setText(article.getContents());
            return convertView;
        }
    }

    private class MyFavoriteAyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (mIsDetails) {
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
            if (!mIsDetails) {
                mAdapter.notifyDataSetChanged();
            } else {
                mArticleAdapter.notifyDataSetChanged();
            }
        }
    }
}
