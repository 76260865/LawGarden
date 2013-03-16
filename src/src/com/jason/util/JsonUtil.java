package com.jason.util;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.News;
import com.jason.lawgarden.model.Subject;
import com.jason.lawgarden.model.UserSubjects;

public class JsonUtil {
    private static final String TAG = "JsonUtil";

    private static final String SERVICE_URI = "http://s-58277.gotocdn.com:8080/Service.svc";

    public static String sAccessToken = "rTacjaF7CUS6qbA2B74Q4Q";

    public static void register() throws JSONException {
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
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, 2, 18);
        sDATE_FOR_TEST = String.format("/Date(%s+0800)/", calendar.getTimeInMillis());

        JSONObject object = new JSONObject();
        object.put("Username", "jason");
        object.put("Password", "123456");

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Login", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getString("AccesToken");
        }
        return null;
    }

    public static void getUserSubjects(Context context) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/GetUserSubjects", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Subjects");
            ArrayList<UserSubjects> subjects = new ArrayList<UserSubjects>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                UserSubjects subject = new UserSubjects();
                subject.setDescription(obj.getString("Description"));
                subject.setId(obj.getInt("Id"));
                subject.setIsPrivate(obj.getInt("IsPrivate"));
                subject.setLastUpdateTime(obj.getString("LastUpdateTime"));
                subject.setName(obj.getString("Name"));
                subject.setOrderId(obj.getInt("OrderId"));
                subject.setParentId(obj.getInt("ParentId"));

                subjects.add(subject);
            }
            // add the subjects to db
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            dbHelper.openDataBase();
            dbHelper.insertUserSubjects(subjects);
        }
    }

    private static String sDATE_FOR_TEST = "/Date(1362575535693+0800)/";

    public static void updateSubjects(Context context) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        object.put("LastUpdateTime", sDATE_FOR_TEST);
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
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            dbHelper.openDataBase();
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

    }

    public static void updateNews(Context context) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        object.put("LastUpdateTime", sDATE_FOR_TEST);
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

                newsList.add(news);
            }
            // add the news to db
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            dbHelper.openDataBase();
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
    }

    public static void updateArticles(Context context) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        object.put("LastUpdateTime", sDATE_FOR_TEST);
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
                article.setLastUpdateTime(obj.getString("Title"));

                articles.add(article);
            }
            // add the articles to db
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            dbHelper.openDataBase();
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
    }
}
