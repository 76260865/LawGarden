package com.jason.lawgarden.db;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jason.lawgarden.R;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;
import com.jason.lawgarden.model.News;
import com.jason.lawgarden.model.Subject;
import com.jason.lawgarden.model.User;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHelper";

    private static final String DB_NAME = "law_garden_db.sqlite";

    private SQLiteDatabase mDataBase;

    // The Android's default system path of your application database.
    private final String mDbPath;

    private final Context mContext;

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     * 
     * @param context
     */
    public DataBaseHelper(Context context) {
        super(context, null, null, 1);
        mContext = context;
        mDbPath = mContext.getFilesDir().getPath() + "/";

        createDataBase();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     * */
    public void createDataBase() {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            // do nothing - database already exist
        } else {

            // By calling this method and empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            getReadableDatabase();

            copyDataBase();
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     * 
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        try {
            String myPath = mDbPath + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            // database does't exist yet.
            Log.d(TAG, e.getMessage());
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     * */
    private void copyDataBase() {
        // Open your local db as the input stream
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.law_garden_db);// .getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = mDbPath + DB_NAME;

        OutputStream moutputStream = null;

        try {
            // Open the empty db as the output stream
            moutputStream = new FileOutputStream(outFileName);

            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                moutputStream.write(buffer, 0, length);
            }

            // Close the streams
            moutputStream.flush();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (moutputStream != null) {
                    moutputStream.close();
                }

                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void openDataBase() throws SQLException {
        // Open the database
        String myPath = mDbPath + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {

        if (mDataBase != null) {
            mDataBase.close();
            Log.d(TAG, "close db");
        }

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the
    // database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd
    // be easy
    // to you to create adapters for your views.

    public void testDb() {
        Cursor cursor = mDataBase.query("user_info", new String[] { "user_name", "service_type" },
                null, null, null, null, null);
        Log.d("SplashActivity", cursor.getCount() + "");
        cursor.close();
        ContentValues values = new ContentValues();
        values.put("user_name", "user_name");
        values.put("service_type", "service_type");
        long id = mDataBase.insertOrThrow("user_info", null, values);
        Log.d("SplashActivity", "row_id:" + id);
        id = mDataBase.insertOrThrow("user_info", null, values);
        Log.d("SplashActivity", "row_id:" + id);
        Cursor cursor1 = mDataBase.query("user_info", new String[] { "user_name", "service_type" },
                null, null, null, null, null);
        Log.d("SplashActivity", cursor1.getCount() + "");
    }

    public static final String[] SUBJECTS_PROJECTION = { "_id", "parent_id", "name", "description",
            "is_new" };

    public ArrayList<Subject> getSubjectsByParentId(int parentId) {
        openDataBase();
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        Subject subject;
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("subjects", SUBJECTS_PROJECTION, "parent_id=" + parentId,
                    null, null, null, null);

            while (cursor.moveToNext()) {
                subject = new Subject();

                subject.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                subject.setParentId(cursor.getInt(cursor.getColumnIndex("parent_id")));
                subject.setName(cursor.getString(cursor.getColumnIndex("name")));
                subject.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                subject.setNew(cursor.getInt(cursor.getColumnIndex("is_new")) == 0 ? false : true);

                subjects.add(subject);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            cursor.close();
        }

        return subjects;
    }

    private static final String SQL_SELECT_ARTICLES_BY_SUBJECTID = "SELECT *, articles_of_law.is_new as new FROM subjects_articles JOIN articles_of_law ON subjects_articles.article_id=articles_of_law._id WHERE subject_id =?";

    public ArrayList<Article> getArticlesBySubjectId(int SubjectId) {
        ArrayList<Article> articles = new ArrayList<Article>();
        Article article;
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery(SQL_SELECT_ARTICLES_BY_SUBJECTID, new String[] { SubjectId
                    + "" });

            while (cursor.moveToNext()) {
                article = new Article();

                article.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                article.setContents(cursor.getString(cursor.getColumnIndex("contents")));
                article.setNew(cursor.getInt(cursor.getColumnIndex("new")) == 0 ? false : true);

                articles.add(article);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            cursor.close();
        }
        return articles;
    }

    private static final String[] NEWS_PROJECTION = { "_id", "title", "content", "create_time",
            "came_from" };

    public ArrayList<News> getAllNews() {
        ArrayList<News> newsList = new ArrayList<News>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        News news = null;
        Cursor cursor = null;

        try {
            cursor = mDataBase
                    .query("news", NEWS_PROJECTION, null, null, null, null, "create_time");
            while (cursor.moveToNext()) {
                news = new News();

                news.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                news.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                news.setContent(cursor.getString(cursor.getColumnIndex("content")));
                news.setCrateTime(format.parse(cursor.getString(cursor
                        .getColumnIndex("create_time"))));
                news.setFrom(cursor.getString(cursor.getColumnIndex("came_from")));

                newsList.add(news);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            cursor.close();
        }
        return newsList;
    }

    private static final String[] FAVORITE_PROJECTION = { "_id", "title", "favorite_type",
            "favorite_id" };

    public ArrayList<Favorite> getAllFavorites() {
        ArrayList<Favorite> favorites = new ArrayList<Favorite>();
        Favorite favorite = null;
        Cursor cursor = null;

        try {
            cursor = mDataBase
                    .query("favorites", FAVORITE_PROJECTION, null, null, null, null, null);
            while (cursor.moveToNext()) {
                favorite = new Favorite();

                favorite.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                favorite.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                favorite.setFavoriteType(cursor.getInt(cursor.getColumnIndex("favorite_type")));
                favorite.setFavoriteId(cursor.getInt(cursor.getColumnIndex("favorite_id")));

                favorites.add(favorite);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            cursor.close();
        }

        return favorites;
    }

    public boolean isFavorited(int favoriteId) {
        boolean isFavorited = false;
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("favorites", FAVORITE_PROJECTION, "favorite_id=" + favoriteId,
                    null, null, null, null);
            isFavorited = cursor.getCount() > 0;
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            cursor.close();
        }
        return isFavorited;
    }

    public Article getArticleById(int id) {
        Article article = new Article();
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("articles_of_law", null, "_id=" + id, null, null, null, null);
            if (cursor.moveToFirst()) {
                article.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                article.setContents(cursor.getString(cursor.getColumnIndex("contents")));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            cursor.close();
        }

        return article;
    }

    public User getUserInfo() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        User user = new User();
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("user_info", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                user.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                user.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
                user.setServiceType(cursor.getInt(cursor.getColumnIndex("service_type")));
                user.setPurchaseDate(format.parse(cursor.getString(cursor
                        .getColumnIndex("purchase_date"))));
                user.setPurchaseDate(new Date());
                user.setOverdueDate(format.parse(cursor.getString(cursor
                        .getColumnIndex("overdue_date"))));
                user.setAboutUs(cursor.getString(cursor.getColumnIndex("about_us")));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            cursor.close();
        }

        return user;
    }

    public void insertSubjects(ArrayList<Subject> subjects) {
        for (Subject subject : subjects) {
            ContentValues values = new ContentValues();
            values.put("_id", subject.getId());
            values.put("parent_id", subject.getParentId());
            values.put("name", subject.getName());
            values.put("description", subject.getDescription());
            values.put("order_id", subject.getOrderId());
            values.put("is_private", subject.isPrivate());
            values.put("last_update_time", subject.getLastUpdateTime());
            values.put("is_new", subject.isNew());

            mDataBase.insert("subjects", null, values);
        }
    }

    public void removeSubjectsByIds(int[] ids) {
        for (int id : ids) {
            mDataBase.delete("subjects", "_id = " + id, null);
        }
    }

    public void insertNews(ArrayList<News> news) {
        for (News newsObj : news) {
            ContentValues values = new ContentValues();
            values.put("_id", newsObj.getId());
            values.put("title", newsObj.getTitle());
            values.put("content", newsObj.getContent());
            values.put("came_from", newsObj.getFrom());
            values.put("source", newsObj.getSource());
            values.put("valid_time", newsObj.getValidTime());
            values.put("last_update_time", newsObj.getLastUpdateTime());

            mDataBase.insert("news", null, values);
        }
    }

    public void removeNewsByIds(int[] ids) {
        for (int id : ids) {
            // mDataBase.delete("news", "_id = " + id, null);
        }
    }

    public void insertArticles(ArrayList<Article> articles) {
        for (Article article : articles) {
            ContentValues values = new ContentValues();
            values.put("_id", article.getId());
            values.put("title", article.getTitle());
            values.put("contents", article.getContents());
            values.put("lastupdatetime", article.getLastUpdateTime());
            values.put("level", article.getLevel());
            values.put("is_new", article.isNew());
            values.put("key_words", article.getKeyWords());
            values.put("subjects", article.getSubjects());

            mDataBase.insert("articles_of_law", null, values);
        }
    }

    public void removeArticlesByIds(int[] ids) {
        for (int id : ids) {
            mDataBase.delete("articles_of_law", "_id = " + id, null);
        }
    }
}