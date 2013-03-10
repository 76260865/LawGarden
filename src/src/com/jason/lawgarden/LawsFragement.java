package com.jason.lawgarden;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Subject;

public class LawsFragement extends Fragment {
    private static final String TAG = "LawsFragement";

    public static final String EXTRA_KEY_SUBJECT_ID = "extra_subject_id";

    public static final String EXTRA_KEY_SUBJECT_NAME = "extra_subject_name";

    private int mSubjectId;

    private DataBaseHelper mDbHelper;

    private ArrayList<Subject> mSubjects = new ArrayList<Subject>();

    private ArrayList<Article> mArticles = new ArrayList<Article>();

    private View mViewSubjectTitle;

    private TextView mTxtSubjectName;

    private String mSubjectName;

    private boolean mIsDetails;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new DataBaseHelper(getActivity());

        Bundle bundle = getArguments();
        mSubjectId = bundle.getInt(EXTRA_KEY_SUBJECT_ID, 0);
        mSubjectName = bundle.getString(EXTRA_KEY_SUBJECT_NAME);

        mSubjects = mDbHelper.getSubjectsByParentId(mSubjectId);
        if (mSubjects.size() == 0) {
            // the last subject, need load the articles
            // mSubjectCallBack.onLastSubjectItemClick(mSubjectId);
            mArticles = mDbHelper.getArticlesBySubjectId(mSubjectId);
            mIsDetails = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.law_list_layout, container, false);
        ListView listLaw = (ListView) view.findViewById(R.id.list_law);
        ListAdapter adapter = mIsDetails ? new LastLawsAdapter() : new LawsAdapter();
        listLaw.setAdapter(adapter);
        listLaw.setOnItemClickListener(mIsDetails ? mOnLastItemClickListener : mOnItemClickListener);

        mViewSubjectTitle = view.findViewById(R.id.linear_subject);
        mTxtSubjectName = (TextView) view.findViewById(R.id.txt_subject_title);
        if (mSubjectId != 0) {
            mViewSubjectTitle.setVisibility(View.VISIBLE);
            mTxtSubjectName.setText(mSubjectName);
        }

        return view;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            Subject subject = mSubjects.get(postion);
            LawsFragement fragment = new LawsFragement();

            Bundle bundle = new Bundle();
            bundle.putInt(LawsFragement.EXTRA_KEY_SUBJECT_ID, subject.getId());
            bundle.putString(LawsFragement.EXTRA_KEY_SUBJECT_NAME, subject.getName());
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

    private OnItemClickListener mOnLastItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int postion, long id) {
            LawDetailsFragement fragment = new LawDetailsFragement();
            Bundle bundle = new Bundle();
            bundle.putInt(LawDetailsFragement.EXTRA_KEY_ARTICLE_ID, mArticles.get(postion).getId());
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

        }
    };

    private class LawsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mSubjects.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
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
            ImageView imgFavorite = (ImageView) convertView.findViewById(R.id.img_favorite);
            imgNew.setImageResource(subject.isNew() ? R.drawable.list_start_sect
                    : R.drawable.list_start);
            new FavoriteAyncTask(imgFavorite, subject.getId()).execute();
            txtTitle.setText(subject.getName());
            return convertView;
        }
    }

    private class LastLawsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mArticles.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
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
            ImageView imgFavorite = (ImageView) convertView.findViewById(R.id.img_favorite);
            imgNew.setImageResource(article.isNew() ? R.drawable.list_start_sect
                    : R.drawable.list_start);
            new FavoriteAyncTask(imgFavorite, article.getId()).execute();
            txtTitle.setText(article.getTitle());
            return convertView;
        }
    }

    private class FavoriteAyncTask extends AsyncTask<Void, Void, Boolean> {

        private ImageView mImgFavorite;

        private int favoriteId;

        FavoriteAyncTask(ImageView imgView, int id) {
            mImgFavorite = imgView;
            favoriteId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mDbHelper.isFavorited(favoriteId);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mImgFavorite.setImageResource(result ? R.drawable.list_start_sect
                    : R.drawable.list_start);
        }
    }
}
