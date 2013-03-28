package com.jason.lawgarden;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.News;

public class NewsOfLawFragment extends Fragment {
    private DataBaseHelper mDbHelper;

    private ArrayList<News> mNewsList = new ArrayList<News>();

    private ListView mListLaw;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new DataBaseHelper(getActivity());
        mDbHelper.openDataBase();
        mNewsList = mDbHelper.getAllNews();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_of_law_layout, null);
        mListLaw = (ListView) view.findViewById(R.id.lst_news);
        mListLaw.setAdapter(new NewsAdapter());
        mListLaw.setOnItemClickListener(mOnItemClickListener);
        return view;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewsDetailsFragment fragment = new NewsDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(NewsDetailsFragment.EXTRA_KEY_NEWS_ID, mNewsList.get(position).getId());
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    };

    private class NewsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNewsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mNewsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.news_of_law_item_layout, null);
            }
            News news = mNewsList.get(position);

            TextView txtTitle = (TextView) convertView.findViewById(R.id.txt_title);
            TextView txtContent = (TextView) convertView.findViewById(R.id.txt_content);
            TextView txtTime = (TextView) convertView.findViewById(R.id.txt_time);
            TextView txtFrom = (TextView) convertView.findViewById(R.id.txt_from);
            ImageView imgNews = (ImageView) convertView.findViewById(R.id.img_new_icon);

            txtTitle.setText(news.getTitle());
            txtContent.setText(news.getContent());
            txtTime.setText(sdf.format(news.getCrateTime()));
            txtFrom.setText(news.getFrom());
            if (news.getBmpByte() != null) {
                imgNews.setImageBitmap(BitmapFactory.decodeByteArray(news.getBmpByte(), 0,
                        news.getBmpByte().length));
            } else {
                new BitmapAyncTask(imgNews, news).execute();
            }

            return convertView;
        }
    }

    private class BitmapAyncTask extends AsyncTask<Void, Void, Bitmap> {

        private ImageView mImageView;
        private News mNews;
        private byte[] bmpByte = null;

        BitmapAyncTask(ImageView imgView, News news) {
            mImageView = imgView;
            mNews = news;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            try {
                bmpByte = getImage(mNews.getUri());
                // saveFile(bitmap, mNews.getId() + ".jpg");
                mNews.setBmpByte(bmpByte);
                mDbHelper.updateNews(mNews);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bmpByte != null) {
                bitmap = BitmapFactory.decodeByteArray(bmpByte, 0, bmpByte.length);// bitmap
            } else {
                Toast.makeText(getActivity(), "Image error!", 1).show();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mNews.setBmpByte(bmpByte);
            mImageView.setImageBitmap(result);
        }

    }

    private byte[] getImage(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        InputStream inStream = conn.getInputStream();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return readStream(inStream);
        }
        return null;
    }

    private static byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    private void saveFile(Bitmap bm, String fileName) throws IOException {
        File dirFile = new File("/sdcard/lawgarden/");
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File("/sdcard/lawgarden/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }
}
