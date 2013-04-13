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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.jason.lawgarden.R;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.Favorite;
import com.jason.lawgarden.model.News;
import com.jason.lawgarden.model.Subject;
import com.jason.lawgarden.model.SubjectArticle;
import com.jason.lawgarden.model.User;
import com.jason.lawgarden.model.UserSubjects;
import com.jason.util.JsonUtil;

@SuppressLint("SimpleDateFormat")
public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHelper";

    private static final String DB_NAME = "law_garden_db.sqlite";

    private SQLiteDatabase mDataBase;

    // The Android's default system path of your application database.
    private final String mDbPath;

    private final Context mContext;

    private static DataBaseHelper mDataBaseHelper;

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     * 
     * @param context
     */
    private DataBaseHelper(Context context) {
        super(context, null, null, 1);
        mContext = context;
        mDbPath = mContext.getFilesDir().getPath() + "/";

        createDataBase();
    }

    public static synchronized DataBaseHelper getSingleInstance(Context context) {
        if (mDataBaseHelper == null) {
            mDataBaseHelper = new DataBaseHelper(context);
            mDataBaseHelper.openDataBase();
        }
        if (!mDataBaseHelper.isOpen()) {
            mDataBaseHelper.openDataBase();
        }
        return mDataBaseHelper;
    }

    private boolean isOpen() {
        return mDataBase.isOpen();
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
            if (cursor != null) {
                cursor.close();
            }
        }

        return subjects;
    }

    public boolean isSubjectAthorized(int userId, int parentId) {
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("user_subject", null, "user_id = ? AND parent_id=?",
                    new String[] { userId + "", parentId + "" }, null, null, null);
            return cursor.getCount() > 0;
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public ArrayList<Subject> getSubjectsByUserId(int userId) {
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        Subject subject;
        Cursor cursor = null;

        try {
            cursor = mDataBase
                    .rawQuery(
                            "SELECT subjects.* FROM subjects JOIN user_subject ON subjects._id=user_subject.parent_id WHERE user_id=?",
                            new String[] { "" + userId });

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
            if (cursor != null) {
                cursor.close();
            }
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
                article.setLastUpdateTime(cursor.getString(cursor
                        .getColumnIndex("last_update_time")));
                article.setNew(cursor.getInt(cursor.getColumnIndex("new")) == 0 ? false : true);

                articles.add(article);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return articles;
    }

    private static final String[] NEWS_PROJECTION = { "_id", "title", "content", "create_time",
            "uri", "came_from", "img_byte" };

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
                news.setUri(cursor.getString(cursor.getColumnIndex("uri")));
                news.setBmpByte(cursor.getBlob(cursor.getColumnIndex("img_byte")));

                newsList.add(news);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return newsList;
    }

    public News getNewsById(int id) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        News news = null;
        Cursor cursor = null;

        try {
            cursor = mDataBase
                    .query("news", NEWS_PROJECTION, null, null, null, null, "create_time");
            if (cursor.moveToFirst()) {
                news = new News();

                news.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                news.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                news.setContent(cursor.getString(cursor.getColumnIndex("content")));
                news.setCrateTime(format.parse(cursor.getString(cursor
                        .getColumnIndex("create_time"))));
                news.setFrom(cursor.getString(cursor.getColumnIndex("came_from")));
                news.setUri(cursor.getString(cursor.getColumnIndex("uri")));
                news.setBmpByte(cursor.getBlob(cursor.getColumnIndex("img_byte")));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return news;
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
                favorite.setFavorited(true);

                favorites.add(favorite);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
            if (cursor != null) {
                cursor.close();
            }
        }
        return isFavorited;
    }

    public boolean isFavoritedByTitle(String title) {
        boolean isFavorited = false;
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("favorites", FAVORITE_PROJECTION, "title='" + title + "'",
                    null, null, null, null);
            isFavorited = cursor.getCount() > 0;
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
            if (cursor != null) {
                cursor.close();
            }
        }

        return article;
    }

    public Article getArticleByTitle(String title) {
        Article article = new Article();
        article.setTitle(title);
        article.setContents("    ");
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("articles_of_law", null, "title='" + title + "'", null, null,
                    null, null);
            while (cursor.moveToNext()) {
                // article.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                // article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                article.setContents(article.getContents()
                        + cursor.getString(cursor.getColumnIndex("contents")));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return article;
    }

    public User getUserInfo(String userName) {
        User user = new User();
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("user_info", null, "user_name='" + userName + "'", null, null,
                    null, null);
            if (cursor.moveToFirst()) {
                user.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                user.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
                user.setServiceType(cursor.getInt(cursor.getColumnIndex("service_type")));
                user.setPurchaseDate(new Date((long) cursor.getDouble(cursor
                        .getColumnIndex("purchase_date"))));
                user.setOverdueDate(new Date((long) cursor.getDouble(cursor
                        .getColumnIndex("overdue_date"))));
                user.setAboutUs(cursor.getString(cursor.getColumnIndex("about_us")));
                user.setToken(cursor.getString(cursor.getColumnIndex("token")));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    public boolean isExistSubject(Subject subject) {
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("subjects", null, "_id=?",
                    new String[] { subject.getId() + "" }, null, null, null);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public void insertSubjects(ArrayList<Subject> subjects) {
        mDataBase.beginTransaction();
        for (Subject subject : subjects) {
            ContentValues values = new ContentValues();
            values.put("_id", subject.getId());
            values.put("parent_id", subject.getParentId());
            values.put("name", subject.getName());
            values.put("description", subject.getDescription());
            values.put("order_id", subject.getOrderId());
            values.put("is_private", subject.isPrivate());
            values.put("last_update_time", subject.getLastUpdateTime());
            values.put("is_new", subject.isNew() ? 1 : 0);

            if (!isExistSubject(subject)) {
                mDataBase.insert("subjects", null, values);
            } else {
                mDataBase.update("subjects", values, "_id = " + subject.getId(), null);
            }
        }

        mDataBase.setTransactionSuccessful();
        mDataBase.endTransaction();
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
            values.put("uri", newsObj.getUri());
            values.put("valid_time", newsObj.getValidTime());
            values.put("last_update_time", newsObj.getLastUpdateTime());

            mDataBase.insert("news", null, values);
        }
    }

    public void removeNewsByIds(int[] ids) {
        for (int id : ids) {
            // TODO:comment this when release
            // mDataBase.delete("news", "_id = " + id, null);
        }
    }

    public boolean isExistArticle(Article article) {
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("articles_of_law", null, "_id=?",
                    new String[] { article.getId() + "" }, null, null, null);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public boolean isExistSubjectArticle(SubjectArticle article) {
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("subjects_articles", null, "article_id=? and subject_id=?",
                    new String[] { article.getArticleId() + "", article.getSubjectId() + "" },
                    null, null, null);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public void insertArticles(ArrayList<Article> articles) {
        mDataBase.beginTransaction();
        for (Article article : articles) {
            ContentValues values = new ContentValues();
            values.put("_id", article.getId());
            values.put("title", article.getTitle());
            values.put("contents", article.getContents());
            values.put("last_update_time", article.getLastUpdateTime());
            values.put("level", article.getLevel());
            values.put("is_new", article.isNew());
            values.put("key_words", article.getKeyWords());
            values.put("subjects", article.getSubjects());

            if (!isExistArticle(article)) {
                mDataBase.insert("articles_of_law", null, values);
            } else {
                mDataBase.update("articles_of_law", values, "_id=" + article.getId(), null);
            }

            String[] subjectIds = article.getSubjects().split(",");
            for (String id : subjectIds) {
                if (TextUtils.isEmpty(id)) {
                    continue;
                }
                ContentValues valuesSubjectArticles = new ContentValues();
                SubjectArticle subjectArticle = new SubjectArticle();
                subjectArticle.setArticleId(article.getId());
                subjectArticle.setSubjectId(Integer.valueOf(id));

                if (!isExistSubjectArticle(subjectArticle)) {
                    valuesSubjectArticles.put("article_id", article.getId());
                    valuesSubjectArticles.put("subject_id", Integer.valueOf(id));

                    mDataBase.insert("subjects_articles", null, valuesSubjectArticles);
                }
            }
        }
        mDataBase.setTransactionSuccessful();
        mDataBase.endTransaction();
    }

    public void removeArticlesByIds(int[] ids) {
        for (int id : ids) {
            mDataBase.delete("articles_of_law", "_id = " + id, null);
        }
    }

    public void updateArticles(Article article) {
        ContentValues values = new ContentValues();
        values.put("_id", article.getId());
        values.put("title", article.getTitle());
        values.put("contents", article.getContents());
        values.put("last_update_time", article.getLastUpdateTime());
        values.put("is_new", article.isNew());
        mDataBase.update("articles_of_law", values, "_id=" + article.getId(), null);
    }

    public void updateSubject(Subject article) {
        ContentValues values = new ContentValues();
        values.put("_id", article.getId());
        values.put("is_new", article.isNew());
        mDataBase.update("subjects", values, "_id=" + article.getId(), null);
    }

    public void addFavorite(Favorite favorite) {
        ContentValues values = new ContentValues();
        values.put("title", favorite.getTitle());
        values.put("favorite_type", favorite.getFavoriteType());
        values.put("favorite_id", favorite.getFavoriteId());
        if (!isExistFavorite(favorite)) {
            mDataBase.insert("favorites", null, values);
        }
    }

    public boolean isExistFavorite(Favorite favorite) {
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("favorites", null, "title='" + favorite.getTitle() + "'",
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public void removeFavoriteByFavoriteIds(int[] ids) {
        for (int id : ids) {
            mDataBase.delete("favorites", "favorite_id = " + id, null);
        }
    }

    public boolean isExistUserSubjects(UserSubjects userSubject) {
        Cursor cursor = null;

        try {
            cursor = mDataBase.query("user_subject", null, "user_id=? and parent_id=?",
                    new String[] { userSubject.getUserId() + "", userSubject.getParentId() + "" },
                    null, null, null);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public void insertUserSubjects(ArrayList<UserSubjects> subjects) {
        for (UserSubjects subject : subjects) {
            ContentValues values = new ContentValues();
            values.put("_id", subject.getId());
            values.put("user_id", JsonUtil.sUser.getId());
            values.put("parent_id", subject.getParentId());
            values.put("name", subject.getName());
            values.put("description", subject.getDescription());
            values.put("order_id", subject.getOrderId());
            values.put("is_private", subject.getIsPrivate());
            values.put("last_update_time", subject.getLastUpdateTime());

            if (!isExistUserSubjects(subject)) {
                mDataBase.insert("user_subject", null, values);
            } else {
                mDataBase.update("user_subject", values, "_id=" + subject.getId(), null);
            }
        }
    }

    public ArrayList<Subject> searchSubjects(String text) {
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        Subject subject;
        Cursor cursor = null;

        try {
            cursor = mDataBase.rawQuery("SELECT * FROM subjects WHERE name LIKE ?",
                    new String[] { "%" + text + "%" });

            while (cursor.moveToNext()) {
                subject = new Subject();

                subject.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                subject.setParentId(cursor.getInt(cursor.getColumnIndex("parent_id")));
                subject.setName(cursor.getString(cursor.getColumnIndex("name")));
                subject.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                subject.setLastUpdateTime(cursor.getString(cursor
                        .getColumnIndex("last_update_time")));
                subject.setNew(cursor.getInt(cursor.getColumnIndex("is_new")) == 0 ? false : true);

                subjects.add(subject);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return subjects;
    }

    public ArrayList<Article> searchArticles(String text) {
        ArrayList<Article> articles = new ArrayList<Article>();
        Article article;
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery(
                    "SELECT * FROM articles_of_law WHERE contents LIKE ? GROUP BY title",
                    new String[] { "%" + text + "%" });

            while (cursor.moveToNext()) {
                article = new Article();

                article.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                article.setContents(cursor.getString(cursor.getColumnIndex("contents")));
                article.setNew(cursor.getInt(cursor.getColumnIndex("is_new")) == 0 ? false : true);
                article.setLastUpdateTime(cursor.getString(cursor
                        .getColumnIndex("last_update_time")));

                articles.add(article);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return articles;
    }

    public ArrayList<Article> searchArticlesByTitle(String text) {
        ArrayList<Article> articles = new ArrayList<Article>();
        Article article;
        Cursor cursor = null;
        try {
            cursor = mDataBase.rawQuery(
                    "SELECT MIN(_id), * FROM articles_of_law WHERE title LIKE ? GROUP BY title",
                    new String[] { "%" + text + "%" });

            while (cursor.moveToNext()) {
                article = new Article();

                article.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                article.setContents(cursor.getString(cursor.getColumnIndex("contents")));
                article.setNew(cursor.getInt(cursor.getColumnIndex("is_new")) == 0 ? false : true);
                article.setLastUpdateTime(cursor.getString(cursor
                        .getColumnIndex("last_update_time")));

                articles.add(article);
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return articles;
    }

    public boolean isExistUser(String userName) {
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = mDataBase.query("user_info", null, "user_name='" + userName + "'", null, null,
                    null, null);

            ret = cursor.getCount() > 0;
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }

    public User insertOrUpdateUser(User user) {
        if (isExistUser(user.getUserName())) {
            ContentValues values = new ContentValues();
            values.put("token", user.getToken());
            values.put("is_remember_pwd", user.isRememberPwd() ? 1 : 0);
            mDataBase.update("user_info", values, "user_name='" + user.getUserName() + "'", null);
        } else {
            ContentValues values = new ContentValues();
            values.put("_id", user.getId());
            values.put("user_name", user.getUserName());
            values.put("token", user.getToken());
            values.put("purchase_date", user.getPurchaseDate().getTime());
            values.put("overdue_date", user.getOverdueDate().getTime());
            values.put("is_remember_pwd", user.isRememberPwd() ? 1 : 0);

            mDataBase.insert("user_info", null, values);
        }
        return getUserInfo(user.getUserName());
    }

    public User getRememberedUser() {
        User user = null;
        Cursor cursor = null;
        try {
            cursor = mDataBase
                    .query("user_info", null, "is_remember_pwd=1", null, null, null, null);

            if (cursor.moveToFirst()) {
                user = new User();

                user.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                user.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
                user.setToken(cursor.getString(cursor.getColumnIndex("token")));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }

    public String getLastUpdateSubjectTime() {
        String lastUpdateTime = "";
        Cursor cursor = null;
        try {
            cursor = mDataBase.query("subjects", null, null, null, null, null,
                    "last_update_time desc");

            if (cursor.moveToFirst()) {
                lastUpdateTime = cursor.getString(cursor.getColumnIndex("last_update_time"));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Last update subject time is :" + lastUpdateTime);
        return lastUpdateTime;
    }

    public String getLastUpdateNewsTime() {
        String lastUpdateTime = "";
        Cursor cursor = null;
        try {
            cursor = mDataBase.query("news", null, null, null, null, null, "last_update_time desc");

            if (cursor.moveToFirst()) {
                lastUpdateTime = cursor.getString(cursor.getColumnIndex("last_update_time"));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Last update subject time is :" + lastUpdateTime);
        return lastUpdateTime;
    }

    public String getLastUpdateArticleTime() {
        String lastUpdateTime = "";
        Cursor cursor = null;
        try {
            cursor = mDataBase.query("articles_of_law", null, null, null, null, null,
                    "last_update_time desc");

            if (cursor.moveToFirst()) {
                lastUpdateTime = cursor.getString(cursor.getColumnIndex("last_update_time"));
            }
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Last update subject time is :" + lastUpdateTime);
        return lastUpdateTime;
    }

    public void updateNews(News newsObj) {
        ContentValues values = new ContentValues();
        values.put("_id", newsObj.getId());
        values.put("img_byte", newsObj.getBmpByte());

        mDataBase.update("news", values, "_id=" + newsObj.get_id(), null);
    }
}