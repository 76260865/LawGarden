package com.jason.util;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.News;
import com.jason.lawgarden.model.Subject;
import com.jason.lawgarden.model.User;
import com.jason.lawgarden.model.UserSubjects;

public class JsonUtil {
    private static final String TAG = "JsonUtil";

    private static final String SERVICE_URI = "http://s-58277.gotocdn.com:8080/Service.svc";

    public static String sAccessToken = "rTacjaF7CUS6qbA2B74Q4Q";

    public static User sUser;

    public static void register() throws JSONException {
        Log.d(TAG, "register");
        JSONObject object = new JSONObject();
        object.put("Username", "jason");
        object.put("Password", "123456");

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Register", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            Log.d(TAG, "register:sucess");
        }
    }

    public static String login(String username, String password) throws JSONException {
        Log.d(TAG, "login");
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 2, 1);
        sDATE_FOR_TEST = String.format("/Date(%s+0800)/", calendar.getTimeInMillis());

        JSONObject object = new JSONObject();
        object.put("Username", username);
        object.put("Password", password);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Login", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getString("AccessToken");
        }
        return null;
    }

    public static void updateUserSubjects(Context context) throws JSONException {
        Log.d(TAG, "updateUserSubjects");
        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/GetUserSubjects", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Subjects");
            ArrayList<UserSubjects> subjects = new ArrayList<UserSubjects>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                UserSubjects subject = new UserSubjects();
                subject.setDescription(obj.getString("Description"));
                // subject.setId(obj.getInt("Id"));
                subject.setIsPrivate(obj.getBoolean("IsPrivate") ? 1 : 0);
                subject.setLastUpdateTime(obj.getString("LastUpdateTime"));
                subject.setName(obj.getString("Name"));
                subject.setOrderId(obj.getInt("OrderId"));
                subject.setParentId(obj.getInt("Id"));

                subjects.add(subject);
            }
            // add the subjects to db
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            dbHelper.openDataBase();
            dbHelper.insertUserSubjects(subjects);
            dbHelper.close();
        }
    }

    private static String sDATE_FOR_TEST = "/Date(1362575535693+0800)/";

    public static void updateSubjects(Context context) throws JSONException {
        Log.d(TAG, "updateSubjects");
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        dbHelper.openDataBase();
        String lastUpdateSubjectTime = dbHelper.getLastUpdateSubjectTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateSubjects", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Subjects");
            ArrayList<Subject> subjects = new ArrayList<Subject>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Subject subject = new Subject();
                subject.setDescription(obj.getString("Description"));
                subject.setId(obj.getInt("Id"));
                subject.setPrivate(obj.getBoolean("IsPrivate"));
                subject.setLastUpdateTime(obj.getString("LastUpdateTime"));
                subject.setName(obj.getString("Name"));
                subject.setOrderId(obj.getInt("OrderId"));
                subject.setParentId(obj.getInt("ParentId"));

                subjects.add(subject);
            }
            // add the subjects to db
            dbHelper.insertSubjects(subjects);

            // remove the subjects:
            JSONArray removedArray = objectRet.getJSONArray("RemovedSubjectIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // remove the subjects by ids
            dbHelper.removeSubjectsByIds(removedIds);
        }
        dbHelper.close();
    }

    public static void updateNews(Context context) throws JSONException {
        Log.d(TAG, "updateNews");
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        dbHelper.openDataBase();
        String lastUpdateSubjectTime = dbHelper.getLastUpdateNewsTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateNews", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("News");
            ArrayList<News> newsList = new ArrayList<News>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                News news = new News();

                news.setId(obj.getInt("Id"));
                news.setTitle(obj.getString("Title"));
                news.setContent(obj.getString("Contents"));
                news.setSource(obj.getString("Source"));
                news.setValidTime(obj.getString("ValidTime"));
                news.setLastUpdateTime(obj.getString("LastUpdateTime"));
                news.setUri(obj.getString("DefaultImageUrl"));

                newsList.add(news);
            }
            // add the news to db
            dbHelper.insertNews(newsList);

            // remove the subjects:
            JSONArray removedArray = objectRet.getJSONArray("RemovedNewsIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // remove the news by ids
            dbHelper.removeNewsByIds(removedIds);
        }
        dbHelper.close();
    }

    public static int updateArticles(Context context, int PageIndex, String lastUpdateSubjectTime)
            throws JSONException {
        Log.d(TAG, "updateArticles");
        DataBaseHelper dbHelper = new DataBaseHelper(context);
        dbHelper.openDataBase();
        // String lastUpdateSubjectTime = dbHelper.getLastUpdateArticleTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);
        object.put("PageIndex", PageIndex);
        object.put("PageSize", 100);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateArticles", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Articles");
            ArrayList<Article> articles = new ArrayList<Article>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Article article = new Article();

                article.setContents(obj.getString("Contents"));
                article.setId(obj.getInt("Id"));
                article.setTitle(obj.getString("Keywords"));
                article.setLastUpdateTime(obj.getString("LastUpdateTime"));
                article.setLevel(obj.getInt("Level"));
                article.setSubjects(obj.getString("Subjects"));
                article.setTitle(obj.getString("Title"));

                articles.add(article);
            }
            // add the articles to db
            dbHelper.insertArticles(articles);

            // remove the articles:
            JSONArray removedArray = objectRet.getJSONArray("RemovedArticleIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // remove the articles by ids
            dbHelper.removeArticlesByIds(removedIds);
        }
        dbHelper.close();

        return objectRet.getInt("TotalPages");
    }
}
