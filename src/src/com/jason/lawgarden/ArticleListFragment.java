package com.jason.lawgarden;

import java.util.ArrayList;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ArticleListFragment extends Fragment {
    private static final String TAG = "ArticleListFragment";
    public static final String EXTRA_KEY_SUBJECT_ID = "extra_subject_id";

    public static final String EXTRA_KEY_SUBJECT_NAME = "extra_subject_name";

    public static final String EXTRA_KEY_SUBJECT_IS_FAVORITED = "extra_subject_is_favorited";

    private int mSubjectId;

    private DataBaseHelper mDbHelper;

    private ListView mListArticle;

    private BaseAdapter mArticleAdapter;

    private ArrayList<Article> mArticles = new ArrayList<Article>();

    private TextView txt_no_data;

    private FragmentManager mFragmentManager;

    private ArticleFragement mArticleFragement;

    private ArticleListFragment mArticleListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = DataBaseHelper.getSingleInstance(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mSubjectId = bundle.getInt(EXTRA_KEY_SUBJECT_ID, -1);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mArticleListFragment = (ArticleListFragment) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article_list);
        mArticleFragement = (ArticleFragement) mFragmentManager
                .findFragmentById(R.id.fragment_detail_article);
        Log.d(TAG, "mArticleFragement:"+mArticleFragement);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_list_layout, container, false);
        txt_no_data = (TextView) view.findViewById(R.id.txt_no_data);
        mListArticle = (ListView) view.findViewById(R.id.list_articles);
        mArticleAdapter = new ArticlesAdapter();
        mListArticle.setAdapter(mArticleAdapter);
        mListArticle.setOnItemClickListener(mOnArticleItemClickListener);
        new ArticlesAsyncTask().execute();
        return view;
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

            txtTitle.setVisibility(View.VISIBLE);
            if (position != 0) {
                if (mArticles.get(position - 1).getTitle().equals(article.getTitle())) {
                    txtTitle.setVisibility(View.GONE);
                }
            }

            txtTitle.setText(article.getTitle());
            txtContent.setText(article.getContents());
            return convertView;
        }
    }

    public void updateContent(int subjectId) {
        mSubjectId = subjectId;
        new ArticlesAsyncTask().execute();
        txt_no_data.setVisibility(View.GONE);
    }

    public void clearContent() {
        mArticles.clear();
        mArticleAdapter.notifyDataSetChanged();
        txt_no_data.setVisibility(View.VISIBLE);
    }

    private OnItemClickListener mOnArticleItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Article article = mArticles.get(postion);
            if (article.isNew()) {
                article.setNew(false);
                mDbHelper.updateArticles(article);
            }

            if (mArticleListFragment != null) {
                mArticleListFragment.getView().setVisibility(View.GONE);
                mArticleFragement.getView().setVisibility(View.VISIBLE);
                mArticleFragement.updateContent(article.getId(), article.getTitle());
            }
        }
    };

    private class ArticlesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mArticles = mDbHelper.getArticlesBySubjectId(mSubjectId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mArticleAdapter.notifyDataSetChanged();
            if (mArticleAdapter.getCount() == 0) {
                txt_no_data.setVisibility(View.VISIBLE);
                txt_no_data.setText("没有法条数据!");
                return;
            } else if (mArticleAdapter.getCount() == 0) {
                txt_no_data.setVisibility(View.VISIBLE);
                txt_no_data.setText("没有法条数据!");
            }
        }
    }
}
