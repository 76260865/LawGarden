package com.jason.lawgarden;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;

public class LawDetailsFragement extends Fragment {

    public static final String EXTRA_KEY_ARTICLE_ID = "extra_key_article_id";

    private TextView mTxtLawContent;

    private TextView mTxtArticleTitle;

    private ImageView imgFavorite;

    private DataBaseHelper mDbHelper;

    private int mArticleId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataBaseHelper(getActivity());
        mArticleId = getArguments().getInt(EXTRA_KEY_ARTICLE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_details_layout, null);
        mTxtLawContent = (TextView) view.findViewById(R.id.txt_article_content);
        mTxtArticleTitle = (TextView) view.findViewById(R.id.txt_article_title);
        imgFavorite = (ImageView) view.findViewById(R.id.img_favorite);
        new ArticleAyncTask().execute();
        return view;
    }

    private class ArticleAyncTask extends AsyncTask<Void, Void, Article> {

        @Override
        protected Article doInBackground(Void... params) {
            mDbHelper.openDataBase();
            Article article = mDbHelper.getArticleById(mArticleId);
            article.setFavorite(mDbHelper.isFavorited(mArticleId));
            return article;
        }

        @Override
        protected void onPostExecute(Article result) {
            imgFavorite.setImageResource(result.isFavorite() ? R.drawable.list_start_sect
                    : R.drawable.list_start);
            mTxtArticleTitle.setText(result.getTitle());
            mTxtLawContent.setText(result.getContents());
        }
    }
}
