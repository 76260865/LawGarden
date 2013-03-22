package com.jason.lawgarden;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

    private DataBaseHelper mDbHelper;

    private Article mArticle;

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
        new ArticleAyncTask().execute();
        return view;
    }

    private class ArticleAyncTask extends AsyncTask<Void, Void, Article> {

        @Override
        protected Article doInBackground(Void... params) {
            mDbHelper.openDataBase();
            mArticle = mDbHelper.getArticleById(mArticleId);
            mArticle.setFavorite(mDbHelper.isFavorited(mArticleId));
            return mArticle;
        }

        @Override
        protected void onPostExecute(Article result) {
            mTxtLawContent.setText(result.getContents());
            setHasOptionsMenu(true);
            // getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setTitle(result.getTitle());
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mArticle == null)
            return true;

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
        item.setIcon(mArticle.isFavorite() ? R.drawable.list_start_sect : R.drawable.list_start);
        getActivity().invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(true);
        menu.getItem(1).setIcon(
                mArticle.isFavorite() ? R.drawable.list_start_sect : R.drawable.list_start);
        // super.onPrepareOptionsMenu(menu);
    }
}
