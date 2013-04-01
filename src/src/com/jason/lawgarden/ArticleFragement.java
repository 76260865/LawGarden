package com.jason.lawgarden;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private int mArticleId;

    private String mArticleTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());
        mArticleId = getArguments().getInt(EXTRA_KEY_ARTICLE_ID);
        mArticleTitle = getArguments().getString(EXTRA_KEY_ARTICLE_TITLE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_details_layout, null);
        mTxtLawContent = (TextView) view.findViewById(R.id.txt_article_content);
        mTxtArticleTitle = (TextView) view.findViewById(R.id.txt_article_title);
        img_article_favorite = (ImageView) view.findViewById(R.id.img_article_favorite);
        mTxtArticleTitle.setText(mArticleTitle);

        img_article_favorite.setOnClickListener(mOnClickListener);

        new ArticleAyncTask().execute();
        return view;
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
        }
    }

}
