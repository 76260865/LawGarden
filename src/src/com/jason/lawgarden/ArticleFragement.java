package com.jason.lawgarden;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;

public class ArticleFragement extends Fragment {

    public static final String EXTRA_KEY_ARTICLE_ID = "extra_key_article_id";
    public static final String EXTRA_KEY_ARTICLE_TITLE = "extra_key_article_title";

    private TextView mTxtLawContent;
    private TextView mTxtArticleTitle;
    private ImageView img_article_favorite;

    private DataBaseHelper mDbHelper;

    private Article mArticle;

    private int mArticleId = -1;

    private String mArticleTitle;
    private View linear_subject;

    private FragmentManager mFragmentManager;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());
        Bundle bundle = getArguments();
        if (bundle != null) {
            mArticleId = bundle.getInt(EXTRA_KEY_ARTICLE_ID);
            mArticleTitle = bundle.getString(EXTRA_KEY_ARTICLE_TITLE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_details_layout, null);
        mTxtLawContent = (TextView) view.findViewById(R.id.txt_article_content);
        mTxtArticleTitle = (TextView) view.findViewById(R.id.txt_article_title);
        img_article_favorite = (ImageView) view.findViewById(R.id.img_article_favorite);
        mTxtArticleTitle.setText(mArticleTitle);

        img_article_favorite.setOnClickListener(mOnClickListener);
        linear_subject = view.findViewById(R.id.linear_subject);
        linear_subject.setVisibility(View.GONE);

        new ArticleAyncTask().execute();
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

    public void updateContent(int articleId, String articleTitle) {
        mArticleId = articleId;
        mArticleTitle = articleTitle;
        mTxtArticleTitle.setText(mArticleTitle);
        linear_subject.setVisibility(View.VISIBLE);
        new ArticleAyncTask().execute();
    }

    public void clearContent() {
        mArticleId = -1;
        mArticleTitle = "";
        mTxtArticleTitle.setText(null);
        mTxtLawContent.setText(null);
        img_article_favorite.setImageBitmap(null);
        linear_subject.setVisibility(View.GONE);
    }

    private ProgressDialog mProgressDialog;
    private boolean mIsCaneled = false;
    private TextView mTxtLoadingInfo;
    private Button mBtnOk;
    private Button mBtnCancel;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressLogin;

    private void initDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.loading_dialog_layout);
        mBtnOk = (Button) mProgressDialog.findViewById(R.id.btn_ok);
        mBtnCancel = (Button) mProgressDialog.findViewById(R.id.btn_cancel);
        mTxtLoadingInfo = (TextView) mProgressDialog.findViewById(R.id.txt_loading_info);
        mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progress_loading);
        mProgressLogin = (ProgressBar) mProgressDialog.findViewById(R.id.progress_login);
        mTxtLoadingInfo.setText("正在加载...");
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mArticle == null)
                return;

            if (!mArticle.isFavorite()) {
                Favorite favorite = new Favorite();
                favorite.setFavoriteId(mArticleId);
                favorite.setTitle(mArticle.getTitle());
                favorite.setFavoriteType(1);
                mDbHelper.addFavorite(favorite);
                mArticle.setFavorite(true);
            } else {
                mDbHelper.removeFavoriteByFavoriteIds(new int[] { mArticleId });
                mArticle.setFavorite(false);
            }
            img_article_favorite
                    .setImageResource(mArticle.isFavorite() ? R.drawable.list_start_sect
                            : R.drawable.list_start);
        }
    };

    private class ArticleAyncTask extends AsyncTask<Void, Void, Article> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mProgressDialog == null) {
                initDialog();
            } else if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        @Override
        protected Article doInBackground(Void... params) {
            mArticle = mDbHelper.getArticleByTitle(mArticleTitle);
            mArticle.setFavorite(mDbHelper.isFavoritedByTitle(mArticleTitle));
            return mArticle;
        }

        @Override
        protected void onPostExecute(Article result) {
            mTxtArticleTitle.setText(result.getTitle());
            mTxtLawContent.setText(result.getContents());
            img_article_favorite
                    .setImageResource(mArticle.isFavorite() ? R.drawable.list_start_sect
                            : R.drawable.list_start);
            linear_subject.setVisibility(View.VISIBLE);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

}
