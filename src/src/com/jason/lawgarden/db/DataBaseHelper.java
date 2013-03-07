package com.jason.lawgarden.db;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
import com.jason.lawgarden.model.Subject;

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
            Log.e(TAG, e.getMessage());
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

    public static final String[] SUBJECTS_PROJECTION = { "_id", "parent_id", "name", "description" };

    public ArrayList<Subject> getSubjectsByParentId(int parentId) {
        openDataBase();
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        Subject subject;

        Cursor cursor = mDataBase.query("subjects", SUBJECTS_PROJECTION, "parent_id=" + parentId,
                null, null, null, null);

        while (cursor.moveToNext()) {
            subject = new Subject();

            subject.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            subject.setParentId(cursor.getInt(cursor.getColumnIndex("parent_id")));
            subject.setName(cursor.getString(cursor.getColumnIndex("name")));
            subject.setDescription(cursor.getString(cursor.getColumnIndex("description")));

            subjects.add(subject);
        }

        return subjects;
    }

    private static final String SQL_SELECT_ARTICLES_BY_SUBJECTID = "SELECT * FROM subjects_articles JOIN articles_of_law ON subjects_articles.article_id=articles_of_law._id WHERE subject_id =?";

    public ArrayList<Article> getArticlesBySubjectId(int SubjectId) {
        ArrayList<Article> articles = new ArrayList<Article>();
        Article article;

        Cursor cursor = mDataBase.rawQuery(SQL_SELECT_ARTICLES_BY_SUBJECTID,
                new String[] { SubjectId + "" });

        while (cursor.moveToNext()) {
            article = new Article();

            article.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            article.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            article.setContents(cursor.getString(cursor.getColumnIndex("contents")));

            articles.add(article);
        }

        return articles;
    }
}